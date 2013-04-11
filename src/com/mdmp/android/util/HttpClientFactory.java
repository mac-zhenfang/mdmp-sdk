package com.mdmp.android.util;


import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class HttpClientFactory {
  
  private static final long IDLE_TIME = 10 * 1000;

  private static HttpClientFactory instance = new HttpClientFactory();
  
  public static HttpClientFactory getInstance(){
    return instance;
  }
  
  private HttpClientFactory(){}
  
  
  public HttpClient createHttpClient(HttpClientConfig config){
    HttpParams params = getHttpParams(config);
    ClientConnectionManager cm = getThreadSafeConnectionManager(params, config);
    AbstractHttpClient client = new DefaultHttpClient(cm, params);
    setKeepAliveStrategy(client, config);
    
    return client;
  }
  
  private static void setKeepAliveStrategy(AbstractHttpClient _client, HttpClientConfig config){
    _client.setKeepAliveStrategy(new ConnectionKeepAliveStrategy(){
      @Override
      public long getKeepAliveDuration(HttpResponse response,HttpContext context) {
            if (response == null) {
                throw new IllegalArgumentException("HTTP response may not be null");
            }
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName(); 
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch(NumberFormatException ignore) {
                    }
                }
            }
            return IDLE_TIME;
        }
      
    });
  }
  
  /**
   * @param params
   * @return
   */
  protected ClientConnectionManager getThreadSafeConnectionManager(HttpParams params, HttpClientConfig config) {
    //Create tcp scheme
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), config.getHttpPort()));
    registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), config.getHttpsPort()));

    //Use thread safe connection manager
    ClientConnectionManager cm = new ThreadSafeClientConnManager(params,registry);
    return cm;
  }
  
  protected HttpVersion getHttpVersion(int version){
    switch(version){
    case 1:
      return HttpVersion.HTTP_1_1;
    default:
      return HttpVersion.HTTP_1_1; 
    }
  }

  /**
   * @return HttpParams
   */
  protected HttpParams getHttpParams(HttpClientConfig config) {
    HttpParams params = new BasicHttpParams();
    //Set http connection params
    HttpConnectionParams.setConnectionTimeout(params, config.getConnectionTimeout());
    HttpConnectionParams.setSocketBufferSize(params, config.getSocketBufferSize());
    HttpConnectionParams.setSoTimeout(params, config.getSoTimeout());
    HttpConnectionParams.setStaleCheckingEnabled(params, config.isStaleCheckingEnabled());
    HttpConnectionParams.setTcpNoDelay(params, config.isTcpNoDelay());

    //set http protocol params
  
    HttpProtocolParams.setVersion(params, getHttpVersion(config.getHttpVersion()));
    HttpProtocolParams.setContentCharset(params, config.getContentCharset());
    HttpProtocolParams.setUseExpectContinue(params, config.isUseExpectContinue());
//    params.setIntParameter(HttpProtocolParams.WAIT_FOR_CONTINUE, 1);
    HttpProtocolParams.setUserAgent(params, config.getUserAgent());

    //set connection manager params
    ConnManagerParams.setMaxConnectionsPerRoute(params,new ConnPerRouteBean(config.getMaxConnectionsPerRoute()));
    // FIXME possible case for Athena Hang issue - If the connection manager (pool) does't have the connection, 
    // the connection manager hang the waiting thread and  await the timeout Mill
    // managedConn = connRequest.getConnection(timeout, TimeUnit.MILLISECONDS);
    // org.apache.http.impl.conn.tsccm.ConnPoolByRoute.getEntryBlocking(ConnPoolByRoute.java:331)
    ConnManagerParams.setTimeout(params, config.getConnectionManagerTimeout());
    ConnManagerParams.setMaxTotalConnections(params, config.getMaxTotalConnections());

    //set auth params
    HttpClientParams.setAuthenticating(params, config.isAuth());
    
    //set auto redirect
    HttpClientParams.setRedirecting(params, config.isAutoRedirect());
    return params;
  }
}




