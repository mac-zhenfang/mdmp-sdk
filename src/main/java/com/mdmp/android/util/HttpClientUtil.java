/*package com.mdmp.android.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.cisco.ramp.api.APIConstants;
import com.cisco.ramp.api.entities.ErrorCode;
import com.cisco.ramp.api.entities.RequestResponse;
import com.webex.webapp.common.util.security.AppTokenUtil;

public class HttpClientUtil {
  
  static Context context = new Context();
  static String appName = context.get("app.name");
  
  private HttpClientUtil() {}
  
  public static HttpClient getDefaultHttpClient() {
    HttpClientFactory factory = HttpClientFactory.getInstance();
    HttpClientConfig config = HttpClientConfig.getDefaultConfig();
    if(timeout > 0){
    	config.setConnectionTimeout(timeout);
    	config.setSoTimeout(timeout);
    }
    return factory.createHttpClient(config);
  }
  
  public static String getStringFromStream(InputStream input) {
    String body = null;
    try {
      ByteArrayOutputStream bao = new ByteArrayOutputStream(1024);
      byte[] bb = new byte[1024];
      int len = 0;
      while ((len = input.read(bb)) > 0) {
        bao.write(bb, 0, len);
      }
      body = new String(bao.toByteArray());
    } catch (Exception e) {
      body = "";
      e.printStackTrace();
    } finally {
      if (input != null) {
        try {
            input.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return body;
  }
  
  public static void closeResponse(HttpResponse response) throws IOException {
    if(null != response && null != response.getEntity()){
      response.getEntity().consumeContent();
    }
  }
  
  private static RequestResponse request(HttpRequestBase method, Map<String, String> header, String content, boolean expectSucc) throws Exception {
    populateParameterAndContent(method, header, content);
    RequestResponse rr = new RequestResponse();
    HttpClient client = HttpClientUtil.getDefaultHttpClient();
    HttpResponse response = client.execute(method);
    
    StatusLine statusLine = response.getStatusLine();
    int status = statusLine.getStatusCode();
    rr.setStatCode(status);
    HttpEntity en = response.getEntity();
    if (status < 300 && expectSucc) {
      String responseStr = HttpClientUtil.getStringFromStream(en.getContent());
      rr.setContent(responseStr);
      return rr;
    } else if (status >= 500 && !expectSucc) {
      String responseStr = HttpClientUtil.getStringFromStream(en.getContent());
      rr.setContent(responseStr);
      return rr;
    } else if(expectSucc){
      String responseStr = HttpClientUtil.getStringFromStream(en.getContent());
      if (status >= 500) {
        ErrorCode code = JsonUtils.convertFrom(responseStr, ErrorCode.class);
        throw new IOException("expectSucc:" + expectSucc + " but HTTP status is " + status + " and " + code);
      } else {
    	  System.out.println("Failed value:" + responseStr);
        en.consumeContent();
        throw new IOException("expectSucc:" + expectSucc + " but HTTP status is " + status + ", URI:" + method.getURI().toString());
      }
      
    }else{
    	String responseStr = HttpClientUtil.getStringFromStream(en.getContent());
        rr.setContent(responseStr);
        return rr;
    }
  }
  
  public static InputStream requestStream(String url, Map<String, String> header, String content, 
                                boolean expectSucc) throws Exception {
    HttpPost method = new HttpPost(url);
    populateParameterAndContent(method, header, content);
    
    HttpClient client = HttpClientUtil.getDefaultHttpClient();
    HttpResponse response = client.execute(method);
    HttpEntity en = response.getEntity();
    
    return en.getContent();
  }
  
  private static void populateParameterAndContent(HttpRequestBase method, Map<String, String> header, 
                                                String content) throws Exception {
    if (header != null && header.size() > 0) {
      String stayAtHeader = context.get("token.in.header", "true");
      boolean shouldConcat = false;
      if (stayAtHeader.equals("true")) {
        method.addHeader(APIConstants.USER_TOKEN, header.get(APIConstants.USER_TOKEN));
      } else {
        method.setURI(new URI(method.getURI() + "?" + APIConstants.USER_TOKEN + "=" 
            + header.get(APIConstants.USER_TOKEN)));
        shouldConcat = true;
      }
      
      if (header.containsKey(APIConstants.FILTER) && header.get(APIConstants.FILTER) != null) {
        if (shouldConcat) {
          method.setURI(new URI(method.getURI() + "&" + APIConstants.FILTER + "=" 
              + header.get(APIConstants.FILTER)));
        } else {
          method.setURI(new URI(method.getURI() + "?" + APIConstants.FILTER + "=" 
              + header.get(APIConstants.FILTER)));
        }
        
        header.remove(APIConstants.FILTER);
      }
      
      if (header.containsKey(APIConstants.SQL) && header.get(APIConstants.SQL) != null) {
        HttpEntityEnclosingRequestBase entityRequestBase = (HttpEntityEnclosingRequestBase) method;
        AbstractHttpEntity reqEntity = new StringEntity(header.get(APIConstants.SQL));
        entityRequestBase.setEntity(reqEntity);
        
        header.remove(APIConstants.SQL);
      }
      
      if (header.size() > 0) {
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        
        for (Map.Entry<String, String> entry : header.entrySet()) {
          if (!entry.getKey().equals(APIConstants.USER_TOKEN) && !entry.getKey().equals(APIConstants.SQL)) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
          }
        }
        
        if (list.size() > 0) {
          HttpPost post = (HttpPost) method;
          post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
        }
      }
    }
    
    if (content != null && !content.trim().isEmpty()) {
      HttpEntityEnclosingRequestBase entityRequestBase = (HttpEntityEnclosingRequestBase) method;
      AbstractHttpEntity reqEntity = new StringEntity(content);
      entityRequestBase.setEntity(reqEntity);
    }
    
    method.addHeader(APIConstants.APP_NAME, appName);
    method.addHeader(APIConstants.APP_TOKEN, AppTokenUtil.makeToken(appName));
  }
  
  public static RequestResponse requestPost(String url, Map<String, String> header, String content, boolean expectSucc) throws Exception {
    return request(new HttpPost(url), header, content, expectSucc);
  }
  
  public static RequestResponse requestPost(String url, Map<String, String> header, boolean expectSucc) throws Exception {
    return request(new HttpPost(url), header, null, expectSucc);
  }
  
  public static RequestResponse requestPut(String url, Map<String, String> header, String content, boolean expectSucc) throws Exception {
    return request(new HttpPut(url), header, content, expectSucc);
  }
  
  public static RequestResponse requestGet(String url, Map<String, String> header, boolean expectSucc) throws Exception {
    return request(new HttpGet(url), header, null, expectSucc);
  }
  
  public static RequestResponse requestDelete(String url, Map<String, String> header, boolean expectSucc) throws Exception {
    return request(new HttpDelete(url), header, null, expectSucc);
  }
  
  private static int timeout = -1;
  private static int DEFAULT_TIMEOUT = 3000;
  public static void setTimeout(int to){
	  timeout = to;
  }
  
}




*/