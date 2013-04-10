 package com.coder.android;
 
 import java.util.TimeZone;
import java.util.UUID;

import com.coder.android.db.DatabaseManager;
import com.coder.android.event.StatEvent;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
 
 public class StatInfo
 {
   private static final String LOG_TAG = "Mofang_MFStatInfo";
   private static SharedPreferences sp = null;
   private static SharedPreferences.Editor spEditor = null;
   public static final String INFO_FILE = "mofang_data_analysis";
   static final String KEY_INSTALL_DATE = "install_date";
   public static final String KEY_DEV_ID = "dev_id";
   static final String KEY_DEV_ID_UNDEFINED = "dev_id_udf";
   public static final String KEY_APP_VERSION = "app_ver";
   static final String KEY_SDK_VERSION = "sdk_ver";
   static final String KEY_CHANNEL = "channel";
   static final String KEY_OS = "os";
   public static final String KEY_OS_VERSION = "os_ver";
   static final String KEY_CARRIER = "carrier";
   static final String KEY_RESOLUTION = "resolution";
   public static final String KEY_MODEL = "model";
   static final String KEY_TIMEZONE = "timezone";
   static final String KEY_LANGUAGE = "language";
   static final String KEY_COUNTRY = "country";
   static final String KEY_DEVICE_ID = "device_id";
   static final String KEY_MAC_ADDRESS = "mac_addr";
   static final String KEY_USER_RETURN = "user_return";
   static final String KEY_USER_ACCESS_PATH = "user_activities";
   static final String KEY_USER_PAUSE_TIME = "user_pause_time";
   static final String KEY_USER_DURATION = "user_duration";
 
   static synchronized void init(Context context)
   {
     if (sp == null) {
       sp = context.getSharedPreferences("mofang_data_analysis", 0);
       spEditor = sp.edit();
     }
   }
 
   static void updateAppAndDeviceInfo(DatabaseManager dbManager, Activity activity, int date, int hour)
   {
	// get device id
     String deviceId = "";
     TelephonyManager telephonyManager = null;
     try {
       telephonyManager = (TelephonyManager)activity.getSystemService("phone");
       deviceId = telephonyManager.getDeviceId().toLowerCase();
       if (deviceId.equals("000000000000000")) {
         deviceId = "";
       }
       putString("device_id", deviceId);
     }
     catch (Exception e)
     {
     }
	 
     putString("model", Build.MODEL);
 
     DisplayMetrics displayMetrics = new DisplayMetrics();
     activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
     putString("resolution", new StringBuilder().append(displayMetrics.heightPixels).append("x").append(displayMetrics.widthPixels).toString());
 
     int installDate = getInt("install_date", 0);
     if (installDate <= 0) {
       installDate = date;
       putInt("install_date", installDate);
     } else {
       boolean isReturnUser = getBoolean("user_return", false);
 
       if ((!isReturnUser) && 
         (date > installDate))
       {
         StatEvent.addEvent(dbManager, "return", date, hour, null);
 
         putBoolean("user_return", true);
       }
 
     }
 
     String sdkVersion = getString("sdk_ver", null);
     if (!"1.0.2".equals(sdkVersion)) {
       putString("sdk_ver", "1.0.2");
       if (sdkVersion != null) {
         StatEvent.addEvent(dbManager, "update", date, hour, "sdk_ver");
       }
 
     }
 
     String oldAppVersion = getString("app_ver", null);
     String appVersion = null;
     try {
       PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 16384);
 
       appVersion = packageInfo.versionName;
     } catch (PackageManager.NameNotFoundException e) {
       Log.e(LOG_TAG, "[updateAppAndDeviceInfo]Get app version error");
       e.printStackTrace();
     }
     if (oldAppVersion == null)
     {
       StatEvent.addEvent(dbManager, "install", date, hour, appVersion);
 
       putString("app_ver", appVersion);
       putInt("install_date", date);
     } else if ((appVersion != null) && (!appVersion.equals(oldAppVersion)))
     {
       StatEvent.addEvent(dbManager, "upgrade", date, hour, oldAppVersion);
 
       StatEvent.addEvent(dbManager, "update", date, hour, "app_ver");
 
       putString("app_ver", appVersion);
 
       putString("user_activities", "");
       StatEvent.clearAccessPath(dbManager);
     }
 
     String channel = getString("channel", null);
     if (channel == null) {
       try {
         channel = (String)activity.getPackageManager().getApplicationInfo(activity.getPackageName(), 128).metaData.get("MOFANG_CHANNEL");
 
         putString("channel", channel);
       } catch (PackageManager.NameNotFoundException e) {
         Log.e(LOG_TAG, "[updateAppAndDeviceInfo]Get app channel error");
         e.printStackTrace();
       }
 
     }
 
     String oldOsVersion = getString("os_ver", null);
     String osVersion = Build.VERSION.RELEASE;
     if ((osVersion != null) && (!osVersion.equals(oldOsVersion))) {
       putString("os_ver", osVersion);
       if (oldOsVersion != null) {
         StatEvent.addEvent(dbManager, "update", date, hour, "os_ver");
       }
 
     }
 
     String oldCarrier = getString("carrier", null);
     String carrier = null;
     if (telephonyManager != null) {
       String imsi = telephonyManager.getSubscriberId();
       if ((imsi != null) && (imsi.trim().length() > 0)) {
         if ((imsi.startsWith("46000")) || (imsi.startsWith("46002")))
           carrier = "中国移动";
         else if (imsi.startsWith("46001"))
           carrier = "中国联通";
         else if (imsi.startsWith("46003"))
           carrier = "中国电信";
         else {
           carrier = telephonyManager.getSimOperatorName();
         }
         if ((carrier != null) && (!carrier.equals(oldCarrier))) {
           putString("carrier", carrier);
           if (oldCarrier != null) {
             StatEvent.addEvent(dbManager, "update", date, hour, "carrier");
           }
         }
       }
 
     }
 
     int oldTimezone = getInt("timezone", 8);
     int timezone = TimeZone.getDefault().getRawOffset() / 3600000;
     if (timezone != oldTimezone) {
       putInt("timezone", timezone);
       StatEvent.addEvent(dbManager, "update", date, hour, "timezone");
     }
 
     String oldLanguage = getString("language", null);
     String language = activity.getResources().getConfiguration().locale.getDisplayLanguage();
     if ((language != null) && (!language.equals(oldLanguage))) {
       putString("language", language);
       if (oldLanguage != null) {
         StatEvent.addEvent(dbManager, "update", date, hour, "language");
       }
 
     }
 
     String oldCountry = getString("country", null);
     String country = activity.getResources().getConfiguration().locale.getCountry();
     if ((country != null) && (!country.equals(oldCountry))) {
       putString("country", country);
       if (oldCountry != null) {
         StatEvent.addEvent(dbManager, "update", date, hour, "country");
       }
 
     }
 
     String devId = getString("dev_id", null);
     if ((devId == null) || (devId.length() != 32))
     {
       WifiManager wifi = (WifiManager)activity.getSystemService("wifi");
       WifiInfo info = wifi.getConnectionInfo();
       String mac = info == null ? "" : (info.getMacAddress() == null ? "" : info.getMacAddress()).replaceAll(":", "").toLowerCase();
 
       putString("mac_addr", mac);
 
       String sum = new StringBuilder().append(deviceId).append("-").append(mac).toString();
       if (sum.length() > 1) {
         sum = new StringBuilder().append("dev-").append(sum).toString();
 
         putInt("dev_id_udf", 0);
       } else {
         sum = new StringBuilder().append("udf-").append(Build.MODEL).append("-").append(osVersion == null ? "" : osVersion)
        		 .append("-").append(carrier == null ? "" : carrier).append("-")
        		 .append(System.currentTimeMillis()).append("-").append(Math.random()).toString();
 
         putInt("dev_id_udf", 1);
       }
       devId = UUID.nameUUIDFromBytes(sum.getBytes()).toString().replaceAll("-", "");
       putString("dev_id", devId);
     } else {
       putInt("dev_id_udf", 0);
     }
   }
 
   static void putString(String key, String value) {
     spEditor.putString(key, value);
     spEditor.commit();
   }
 
   public static String getString(String key, String def)
   {
     return sp.getString(key, def);
   }
 
   static void putInt(String key, int value) {
     spEditor.putInt(key, value);
     spEditor.commit();
   }
 
   static int getInt(String key, int def)
   {
     return sp.getInt(key, def);
   }
 
   static void putLong(String key, long value) {
     spEditor.putLong(key, value);
     spEditor.commit();
   }
 
   static long getLong(String key, long def)
   {
     return sp.getLong(key, def);
   }
 
   static void putBoolean(String key, boolean value)
   {
     spEditor.putBoolean(key, value);
     spEditor.commit();
   }
 
   static boolean getBoolean(String key, boolean def)
   {
     return sp.getBoolean(key, def);
   }
 }
