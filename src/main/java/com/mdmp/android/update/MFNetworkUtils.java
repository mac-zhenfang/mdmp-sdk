 package com.mdmp.android.update;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.State;
 import android.net.wifi.WifiManager;
 
 public class MFNetworkUtils
 {
   public static final int STATE_NONE = 0;
   public static final int STATE_WIFI = 1;
   public static final int STATE_MOBILE = 2;
   public static final int WIFI_OFF = -1;
   public static final int WIFI_ON = 0;
   private static WifiManager wifiManager;
 
   public static int getWifiIsEnabled(Context context)
   {
     wifiManager = (WifiManager)context.getSystemService("wifi");
     if (wifiManager.isWifiEnabled()) {
       return 0;
     }
     return -1;
   }
 
   public static boolean isNetworkAvailable(Context context)
   {
     boolean netWorkStatus = false;
 
     ConnectivityManager connManager = (ConnectivityManager)context.getSystemService("connectivity");
 
     if (connManager.getActiveNetworkInfo() != null) {
       netWorkStatus = connManager.getActiveNetworkInfo().isAvailable();
     }
 
     return netWorkStatus;
   }
 
   public static int getNetworkState(Context context)
   {
     ConnectivityManager connManager = (ConnectivityManager)context.getSystemService("connectivity");
 
     NetworkInfo.State state = connManager.getNetworkInfo(1).getState();
     if ((state == NetworkInfo.State.CONNECTED) || (state == NetworkInfo.State.CONNECTING)) {
       return 1;
     }
 
     state = connManager.getNetworkInfo(0).getState();
     if ((state == NetworkInfo.State.CONNECTED) || (state == NetworkInfo.State.CONNECTING)) {
       return 2;
     }
 
     return 0;
   }
 }
