 package com.mdmp.android.update;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.zip.GZIPInputStream;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 public class MFHttpUtils
 {
   private static final String ACCEPT_ENCODING = "gzip, deflate";
   public static int CONNECT_TIMEOUT = 20;
 
   public static int DATA_TIMEOUT = 40;
 
   public static String invokeText(String urlStr)
     throws IOException
   {
     StringBuffer textBuff = new StringBuffer();
     HttpGet request = new HttpGet(urlStr);
     request.addHeader("Accept-Encoding", ACCEPT_ENCODING);
 
     HttpParams params = new BasicHttpParams();
     HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT * 1000);
     HttpConnectionParams.setSoTimeout(params, DATA_TIMEOUT * 1000);
     HttpClient httpClient = new DefaultHttpClient(params);
 
     BufferedReader reader = null;
     try {
       HttpResponse response = httpClient.execute(request);
       if (response.getStatusLine().getStatusCode() == 200) {
         InputStream is = response.getEntity().getContent();
         Header contentEncoding = response.getFirstHeader("Content-Encoding");
         if ((contentEncoding != null) && (contentEncoding.getValue().equalsIgnoreCase("gzip"))) {
           is = new GZIPInputStream(is);
         }
         reader = new BufferedReader(new InputStreamReader(is));
         String line;
         while ((line = reader.readLine()) != null)
           textBuff.append(line).append("\r\n");
       }
     }
     finally {
       if (reader != null) {
         reader.close();
       }
     }
     return textBuff.toString();
   }
 }
