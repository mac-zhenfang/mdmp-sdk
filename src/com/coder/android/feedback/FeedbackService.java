 package com.coder.android.feedback;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import com.coder.android.StatInfo;
 import com.coder.android.DataAgent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class FeedbackService
 {
   private static final String ACCEPT_ENCODING = "gzip, deflate";
   private static int CONNECT_TIMEOUT = 20;
   private static int DATA_TIMEOUT = 40;
 
   private static String FEEDBACK_URL = "http://m.imofan.com/feedbacks/?app_key=";
   private static String UUID_URL = "http://m.imofan.com/uuid.jsp";
   private static final int SUBMIT_FAILED = -1;
   private static final String SP_FILE_NAME = "mffeedbacks";
 
   public static void submit(Activity activity, Feedback feedback, FeedbackSubmitListener feedbackSubmitListener)
   {
     Activity activity1 = activity;
     Feedback feedBack1 = feedback;
     FeedbackSubmitListener feedbackSubmitListener1 = feedbackSubmitListener;
 
     new Thread(new Runnable() {
       public void run() {
         if ((this.val$activity1 == null) || (this.val$feedBack1 == null) || (this.val$feedBack1.getFeedback().length() > 500) || (this.val$feedbackSubmitListener1 == null))
         {
           this.val$feedbackSubmitListener1.onSubmitFailed();
           return;
         }
 
         String backJson = null;
         Map maps = null;
         backJson = FeedbackService.access$000(this.val$activity1, this.val$feedBack1);
 
         if (("".equals(backJson)) || (backJson == null)) {
           this.val$feedbackSubmitListener1.onSubmitFailed();
           return;
         }
 
         maps = FeedbackService.access$100(backJson);
         if (maps == null) {
           this.val$feedbackSubmitListener1.onSubmitFailed();
           return;
         }
 
         if (Integer.parseInt((String)maps.get("status")) == -1) {
           this.val$feedbackSubmitListener1.onSubmitFailed();
           return;
         }
 
         FeedbackService.update(this.val$activity1, new FeedbackReplyListener()
         {
           public void onDetectedNothing()
           {
           }
 
           public void onDetectedNewReplies(List<Feedback> feedbacks)
           {
           }
         });
         this.val$feedbackSubmitListener1.onSubmitSucceeded(this.val$feedBack1);
       }
     }).start();
   }
 
   public static List<Feedback> getLocalAllReply(Activity activity)
   {
     List<Feedback> list = new ArrayList<Feedback>();
     SharedPreferences preferences = activity.getSharedPreferences("mffeedbacks", 0);
     String json = preferences.getString("json", null);
 
     if (json != null) {
       try {
         JSONObject jsonObj = new JSONObject(json);
         JSONArray jsonArray = jsonObj.getJSONArray("feedbacks");
         for (int i = 0; i < jsonArray.length(); i++) {
           Feedback fd = new Feedback(jsonArray.getJSONObject(i).getString("feedback"));
           fd.setUserType(jsonArray.getJSONObject(i).getInt("by"));
           fd.setTimestamp(jsonArray.getJSONObject(i).getString("timestamp"));
           list.add(fd);
         }
       } catch (JSONException e) {
         printErr(e);
         return list;
       }
     }
 
     return list;
   }
 
   public static void update(Activity activity, FeedbackReplyListener feedbackReplyListener)
   {
     Activity activity1 = activity;
     FeedbackReplyListener feedbackReplyListener1 = feedbackReplyListener;
 
     new Thread(new Runnable()
     {
       public void run() {
         if ((this.val$activity1 == null) || (this.val$feedbackReplyListener1 == null)) {
           this.val$feedbackReplyListener1.onDetectedNothing();
           return;
         }
 
         SharedPreferences preferences = this.val$activity1.getSharedPreferences("mffeedbacks", 0);
         String timestamp = preferences.getString("timestamp", "0");
 
         String backJson = FeedbackService.access$200(this.val$activity1, timestamp);
         if (("".equals(backJson)) || (backJson == null)) {
           this.val$feedbackReplyListener1.onDetectedNothing();
           return;
         }
         List list = FeedbackService.access$300(backJson);
         if (list == null) {
           this.val$feedbackReplyListener1.onDetectedNothing();
           return;
         }
 
         FeedbackService.access$400(this.val$activity1, backJson);
         this.val$feedbackReplyListener1.onDetectedNewReplies(list);
       }
     }).start();
   }
 
   private static Map<String, String> getSubmitBackInfo(String json)
   {
     Map maps = new HashMap();
     try
     {
       JSONObject jsonObj = new JSONObject(json);
       maps.put("status", jsonObj.getString("status"));
       maps.put("desc", jsonObj.getString("desc"));
       maps.put("at", jsonObj.getString("at"));
     } catch (JSONException e) {
       printErr(e);
       return null;
     }
 
     if (maps.isEmpty())
       maps = null;
     return maps;
   }
 
   private static List<Feedback> getUpdateBackInfo(String json)
   {
     List list = new ArrayList();
     try
     {
       JSONObject jsonObj = new JSONObject(json);
       JSONArray jsonArray = jsonObj.getJSONArray("feedbacks");
       for (int i = 0; i < jsonArray.length(); i++) {
         JSONObject json1 = jsonArray.getJSONObject(i);
         Feedback fb = new Feedback(json1.getString("text"));
         fb.setUserType(json1.getInt("by"));
         fb.setTimestamp(json1.getString("at"));
         list.add(fb);
       }
     } catch (JSONException e) {
       printErr(e);
       return null;
     }
     if (list.isEmpty())
       list = null;
     return list;
   }
 
   private static void saveReply(Activity activity, String json)
   {
     SharedPreferences preferences = activity.getSharedPreferences("mffeedbacks", 0);
     SharedPreferences.Editor editor = preferences.edit();
 
     List list = getUpdateBackInfo(json);
     editor.putString("timestamp", ((Feedback)list.get(list.size() - 1)).getTimestamp());
     editor.commit();
 
     String localReply = preferences.getString("json", null);
     if (localReply == null)
       try
       {
         JSONObject jsonObj = new JSONObject();
         JSONArray jsonArray = new JSONArray();
 
         for (int i = 0; i < list.size(); i++) {
           JSONObject jo = new JSONObject();
           jo.put("by", ((Feedback)list.get(i)).getUserType());
           jo.put("feedback", ((Feedback)list.get(i)).getFeedback());
           jo.put("timestamp", ((Feedback)list.get(i)).getTimestamp());
           jsonArray.put(jo);
         }
         jsonObj.put("feedbacks", jsonArray);
         editor.putString("json", jsonObj.toString());
         editor.commit();
       } catch (JSONException e) {
         printErr(e);
       }
     else
       try
       {
         JSONObject jsonObj = new JSONObject(preferences.getString("json", null));
         JSONArray jsonArray = jsonObj.getJSONArray("feedbacks");
         for (int i = 0; i < list.size(); i++) {
           JSONObject jo = new JSONObject();
           jo.put("by", ((Feedback)list.get(i)).getUserType());
           jo.put("feedback", ((Feedback)list.get(i)).getFeedback());
           jo.put("timestamp", ((Feedback)list.get(i)).getTimestamp());
           jsonArray.put(jo);
         }
         editor.putString("json", jsonObj.toString());
         editor.commit();
       } catch (JSONException e) {
         printErr(e);
       }
   }
 
   private static String getSubmitResponseWithPost(Activity activity, Feedback mffeedBack)
   {
     DataAgent.init(activity);
 
     String SUBMIT_CONTENT_URL = FEEDBACK_URL;
 
     HttpParams param = new BasicHttpParams();
     HttpConnectionParams.setConnectionTimeout(param, CONNECT_TIMEOUT * 1000);
     HttpConnectionParams.setSoTimeout(param, DATA_TIMEOUT * 1000);
 
     HttpClient httpClient = new DefaultHttpClient(param);
 
     SUBMIT_CONTENT_URL = SUBMIT_CONTENT_URL + DataAgent.getAppKey(activity);
 
     HttpPost post = new HttpPost(SUBMIT_CONTENT_URL);
     post.setHeader("Accept-Encoding", "gzip, deflate");
 
     List params = new ArrayList();
     params.add(new BasicNameValuePair("dev_id", getUUID(activity)));
     params.add(new BasicNameValuePair("app_ver", StatInfo.getString("app_ver", "")));
     params.add(new BasicNameValuePair("os_ver", StatInfo.getString("os_ver", "")));
     params.add(new BasicNameValuePair("model", StatInfo.getString("model", "")));
     params.add(new BasicNameValuePair("user_info", mffeedBack.getUserInfo()));
     params.add(new BasicNameValuePair("text", mffeedBack.getFeedback()));
     try
     {
       post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
       HttpResponse response = httpClient.execute(post);
       if (response.getStatusLine().getStatusCode() == 200) {
         InputStream is = response.getEntity().getContent();
         if (is == null) {
           return null;
         }
         Header contentEncoding = response.getFirstHeader("Content-Encoding");
         if ((contentEncoding != null) && (contentEncoding.getValue().equalsIgnoreCase("gzip"))) {
           is = new GZIPInputStream(is);
         }
         BufferedReader reader = null;
         String line = "";
         StringBuffer content = new StringBuffer();
         try {
           reader = new BufferedReader(new InputStreamReader(is));
           while ((line = reader.readLine()) != null)
             content.append(line).append("\r\n");
         }
         finally {
           if (reader != null) {
             reader.close();
           }
           if (is != null) {
             is.close();
           }
         }
         return content.toString();
       }
     } catch (UnsupportedEncodingException e) {
       printErr(e);
       return null;
     } catch (ClientProtocolException e) {
       printErr(e);
       return null;
     } catch (IOException e) {
       printErr(e);
       return null;
     }
     return null;
   }
 
   private static String getUUID(Activity activity)
   {
     SharedPreferences preferences = activity.getSharedPreferences("mffeedbacks", 0);
     String UUID = preferences.getString("UUID", null);
     if (UUID != null) {
       return UUID;
     }
 
     try
     {
       URL url = new URL(UUID_URL);
       HttpURLConnection conn = (HttpURLConnection)url.openConnection();
       conn.setRequestMethod("GET");
       conn.setReadTimeout(5000);
       InputStreamReader isr = new InputStreamReader(conn.getInputStream());
       BufferedReader br = new BufferedReader(isr);
       UUID = br.readLine();
       SharedPreferences.Editor editor = preferences.edit();
       editor.putString("UUID", UUID);
       editor.commit();
     } catch (MalformedURLException e) {
       printErr(e);
       return null;
     } catch (IOException e) {
       printErr(e);
       return null;
     }
     return UUID;
   }
 
   private static String getFbRspWithGet(Activity activity, String ts)
   {
     DataAgent.init(activity);
 
     String GET_FEEDBACK_URL = FEEDBACK_URL + DataAgent.getAppKey(activity) + "&dev_id=" + getUUID(activity) + "&ts=" + ts;
 
     StringBuffer textBuff = new StringBuffer();
     HttpGet request = new HttpGet(GET_FEEDBACK_URL);
     request.addHeader("Accept-Encoding", "gzip, deflate");
 
     HttpParams params = new BasicHttpParams();
     HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT * 1000);
     HttpConnectionParams.setSoTimeout(params, DATA_TIMEOUT * 1000);
     HttpClient httpClient = new DefaultHttpClient(params);
     InputStream is = null;
     BufferedReader reader = null;
     try {
       HttpResponse response = httpClient.execute(request);
       if (response.getStatusLine().getStatusCode() == 304) {
         return null;
       }
       is = response.getEntity().getContent();
       Header contentEncoding = response.getFirstHeader("Content-Encoding");
       if ((contentEncoding != null) && (contentEncoding.getValue().equalsIgnoreCase("gzip"))) {
         is = new GZIPInputStream((InputStream)is);
       }
       reader = new BufferedReader(new InputStreamReader((InputStream)is));
       String line;
       while ((line = reader.readLine()) != null)
         textBuff.append(line).append("\r\n");
     }
     catch (ClientProtocolException e) {
       printErr(e);
       return null;
     }
     catch (IOException e)
     {
       printErr(e);
       return null;
     }
     finally
     {
       if (reader != null) {
         try {
           reader.close();
         } catch (IOException e) {
           e.printStackTrace();
         }
       }
     }
     return (String)textBuff.toString();
   }
 
   private static void printErr(Exception e) {
     if (DataAgent.isDebug())
       e.printStackTrace();
   }
 }
