 package com.coder.android.notification;
 
 import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
 
 public class MFPushUtils
 {
   private static final String TAG = "MFPushUtils";
   private static final String ALL_LANGUAGE = "all";
 
   static String pullMsgByPost(MFParameter mfParameter)
   {
     String url = "http://apns.imofan.com/pull/";
     StringBuilder sb = new StringBuilder(url);
     sb.append(mfParameter.getDevId());
     sb.append("/" + String.valueOf(mfParameter.getAppId()));
     sb.append("/" + String.valueOf(mfParameter.getTimeStamp()));
     sb.append("/" + String.valueOf(mfParameter.getMd5()));
     url = sb.toString();
 
     String jsonData = getJsonFromServer(url);
 
     return jsonData;
   }
 
   private static String getJsonFromServer(String uri)
   {
     String message = null;
     HttpPost post = new HttpPost(uri);
     try {
       HttpParams param = new BasicHttpParams();
       SchemeRegistry schReg = new SchemeRegistry();
       schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
       schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
       ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(param, schReg);
       DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, param);
       HttpResponse response = httpClient.execute(post);
       int responseStatusCode = response.getStatusLine().getStatusCode();
 
       if ((responseStatusCode == 201) || (responseStatusCode == 200))
       {
         message = EntityUtils.toString(response.getEntity(), "utf-8");
       }
       else message = null;
 
     }
     catch (IOException e)
     {
       message = null;
       e.printStackTrace();
     }
     catch (Exception e) {
       message = null;
       e.printStackTrace();
     } finally {
       post.abort();
     }
     return message;
   }
 
   static List<MFMessage> parseJson(String msgContent)
   {
     List msgs = null;
     if ((null != msgContent) && (!"".equals(msgContent))) {
       try {
         JSONObject jsonObject = new JSONObject(msgContent);
         JSONArray jsonArray = jsonObject.optJSONArray("messages");
         if ((null != jsonArray) && (jsonArray.length() > 0)) {
           msgs = new ArrayList();
           int length = jsonArray.length();
           for (int i = 0; i < length; i++) {
             MFMessage mfMessage = new MFMessage();
             mfMessage.setAppId(jsonArray.optJSONObject(i).optString("app_id"));
             mfMessage.setAppVersion(jsonArray.optJSONObject(i).optString("app_ver"));
             mfMessage.setMsgContent(jsonArray.optJSONObject(i).optString("content"));
             mfMessage.setLanguage(jsonArray.optJSONObject(i).optString("lang"));
             mfMessage.setOsVersion(jsonArray.optJSONObject(i).optString("os_ver"));
             mfMessage.setMsgId(jsonArray.optJSONObject(i).optInt("push_id"));
             mfMessage.setTimeStamp(jsonArray.optJSONObject(i).optLong("ts"));
             msgs.add(mfMessage);
           }
         }
       } catch (JSONException e) {
         msgs = null;
         e.printStackTrace();
       }
     }
     return msgs;
   }
 
   static MFMessage getMaxTsMsg(Context context, List<MFMessage> msgs)
   {
     if ((null != msgs) && (msgs.size() > 0)) {
       int size = msgs.size();
       int k = 0;
       long max = ((MFMessage)msgs.get(k)).getTimeStamp();
       for (int i = 0; i < size; i++) {
         if (max < ((MFMessage)msgs.get(i)).getTimeStamp()) {
           k = i;
           max = ((MFMessage)msgs.get(i)).getTimeStamp();
         }
 
       }
 
       MFMessage mfMessage = (MFMessage)msgs.get(k);
 
       PreferencesUtils.setPreferences(context, "message", "latestTime", mfMessage.getTimeStamp());
 
       boolean appVerCompareResult = compareAppVersion(context, mfMessage);
 
       boolean osVerCompareResult = compareOSVersion(mfMessage);
 
       boolean langCompareResult = compareLaunage(context, mfMessage);
 
       if ((appVerCompareResult) && (osVerCompareResult) && (langCompareResult)) {
         return (MFMessage)msgs.get(k);
       }
     }
     return null;
   }
 
   private static String getOsVersion()
   {
     String version = Build.VERSION.RELEASE;
     if ((null != version) && (!"".equals(version))) {
       return version.substring(0, version.lastIndexOf("."));
     }
     return null;
   }
 
   private static int getAppVersion(Context context)
   {
     PackageInfo pinfo = null;
     try {
       pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 16384);
 
       if (null != pinfo)
         return pinfo.versionCode;
     }
     catch (PackageManager.NameNotFoundException e) {
       e.printStackTrace();
     }
 
     return 0;
   }
 
   private static boolean compareAppVersion(Context context, MFMessage mfMessage)
   {
     boolean result = false;
     if (null != mfMessage)
     {
       String appVersion = mfMessage.getAppVersion();
 
       int loacalAppVersion = getAppVersion(context);
       float localVer = Float.parseFloat(String.valueOf(loacalAppVersion));
 
       result = comparator(localVer, appVersion);
     }
     return result;
   }
 
   private static boolean compareOSVersion(MFMessage mfMessage)
   {
     boolean result = false;
     if (null != mfMessage)
     {
       String osVersion = mfMessage.getOsVersion();
 
       String localOSVersion = getOsVersion();
 
       result = comparator(Float.valueOf(localOSVersion).floatValue(), osVersion);
     }
     return result;
   }
 
   private static boolean compareLaunage(Context context, MFMessage mfMessage)
   {
     boolean result = false;
     if (null != mfMessage)
     {
       String localLanguage = context.getResources().getConfiguration().locale.getDisplayLanguage().toLowerCase();
 
       String serverLanguage = mfMessage.getLanguage();
 
       if ("all".equals(serverLanguage))
         result = true;
       else if (localLanguage.equals(serverLanguage)) {
         result = true;
       }
     }
     return result;
   }
 
   private static boolean comparator(float value, String msg)
   {
     boolean result = false;
     float version = -1.0F;
 
     if ("".equals(msg)) {
       result = true;
     }
     else if (null != msg) {
       if ((msg.contains(">=")) && (msg.indexOf("=") > 0)) {
         version = getVersion(msg, "=");
         if ((version != -1.0F) && (value >= version))
           result = true;
       }
       else if ((msg.contains("<=")) && (msg.indexOf("=") > 0)) {
         version = getVersion(msg, "=");
         if ((version != -1.0F) && (value <= version))
           result = true;
       }
       else if (msg.contains(">")) {
         version = getVersion(msg, ">");
         if ((version != -1.0F) && (value > version))
           result = true;
       }
       else if (msg.contains("<")) {
         version = getVersion(msg, "<");
         if ((version != -1.0F) && (value < version))
           result = true;
       }
       else if (msg.contains("=")) {
         version = getVersion(msg, "=");
         if ((version != -1.0F) && (value == version)) {
           result = true;
         }
       }
     }
     return result;
   }
 
   private static float getVersion(String msg, String sign)
   {
     float version = -1.0F;
     if ((null != msg) && (!"".equals(msg)) && (null != sign) && (!"".equals(sign))) {
       String ver = msg.substring(msg.indexOf(sign) + 1, msg.length());
       if ((null != ver) && (!"".equals(ver))) {
         version = Float.valueOf(ver).floatValue();
       }
     }
     return version;
   }
 
   static void saveListenerCLass(Context context, Class listenerClass)
   {
     if (null != listenerClass) {
       String listenerName = listenerClass.getName();
       if ((null != listenerName) && (!"".equals(listenerName)))
         PreferencesUtils.setPreferences(context, "mfpush", "pushListener", listenerName);
     }
   }
 
   static MFNotificationListener getNotificationListener(Context context)
   {
     MFNotificationListener mfNotificationListener = null;
     String listenerName = PreferencesUtils.getPreference(context, "mfpush", "pushListener", "");
     if ((listenerName != null) && (listenerName.trim().length() > 0)) {
       try {
         mfNotificationListener = (MFNotificationListener)(MFNotificationListener)Class.forName(listenerName).newInstance();
       } catch (IllegalAccessException e) {
         mfNotificationListener = null;
         e.printStackTrace();
       } catch (InstantiationException e) {
         mfNotificationListener = null;
         e.printStackTrace();
       } catch (ClassNotFoundException e) {
         mfNotificationListener = null;
         e.printStackTrace();
       } catch (ClassCastException e) {
         mfNotificationListener = null;
         e.printStackTrace();
       } catch (Exception e) {
         mfNotificationListener = null;
         e.printStackTrace();
       }
     }
     return mfNotificationListener;
   }
 
   static String getDevId(Context context)
   {
     String devId = null;
     devId = PreferencesUtils.getPreference(context, "mofang_data_analysis", "dev_id", "");
     return devId;
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.notification.MFPushUtils
 * JD-Core Version:    0.6.0
 */
