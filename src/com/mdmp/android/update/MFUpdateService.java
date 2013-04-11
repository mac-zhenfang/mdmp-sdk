 package com.mdmp.android.update;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Application;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.widget.RemoteViews;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public class MFUpdateService extends Service
 {
   private static String CHECKURL = null;
   private static final int DOWNLOAD_COMPLETE = 0;
   private static final int DOWNLOAD_URL_ERROR = 1;
   private static final int DOWNLOAD_FAIL = 2;
   private static final int CREATE_FILE_FAIL = 3;
   private static MFAPPInfo mfappInfo;
   private String updateNotificationTitle;
   public static File updateFile = null;
   private static PendingIntent updatePendingIntent = null;
   private static int updateTotalSize = 0;
   private NotificationManager updateNotificationManager = null;
   private Notification updateNotification = null;
 
   private static int iconID = 0;
   private static int appNameID = 0;
   private static int layoutID;
   private static int textViewID;
   private static int blockViewID;
   private static int processbarViewID;
   private static int notificationBarIconViewID;
   private static int notificationBarAppNameViewID;
   public static File internalFileDir;
   public static int updateApkVersionCode;
   private static String updateApkDownloadUrl;
   private Handler updateHandler = new Handler()
   {
     public void handleMessage(Message msg) {
       switch (msg.what)
       {
       case 0:
         MFUpdateService.this.updateNotification.tickerText = "下载完毕！";
         MFUpdateService.this.updateNotification.contentView.setViewVisibility(MFUpdateService.blockViewID, 8);
         MFUpdateService.this.updateNotificationManager.notify(MFUpdateService.iconID, MFUpdateService.this.updateNotification);
 
         String cmd = "chmod 777 " + MFUpdateService.updateFile.getPath();
         try {
           Runtime.getRuntime().exec(cmd);
         } catch (IOException e) {
           e.printStackTrace();
         }
 
         Uri uri = Uri.fromFile(MFUpdateService.updateFile);
         Intent updateCompleteIntent = new Intent("android.intent.action.VIEW");
         updateCompleteIntent.setDataAndType(uri, "application/vnd.android.package-archive");
 
         MFUpdateService.access$1102(PendingIntent.getActivity(MFUpdateService.this, MFUpdateService.appNameID, updateCompleteIntent, 134217728));
 
         MFUpdateService.this.updateNotification.defaults = 1;
         MFUpdateService.this.updateNotification.contentIntent = MFUpdateService.updatePendingIntent;
         MFUpdateService.this.updateNotification.contentView.setTextViewText(MFUpdateService.textViewID, "下载完成，点击安装！");
         MFUpdateService.this.updateNotification.flags |= 16;
         MFUpdateService.this.updateNotificationManager.notify(MFUpdateService.iconID, MFUpdateService.this.updateNotification);
         break;
       case 1:
         MFUpdateService.this.updateNotification.setLatestEventInfo(MFUpdateService.this, MFUpdateService.this.updateNotificationTitle, "下载失败 ，请重新下载！", MFUpdateService.updatePendingIntent);
 
         MFUpdateService.this.updateNotificationManager.notify(MFUpdateService.iconID, MFUpdateService.this.updateNotification);
         break;
       case 3:
         MFUpdateService.this.updateNotification.setLatestEventInfo(MFUpdateService.this, MFUpdateService.this.updateNotificationTitle, "下载失败，请检测内部存储是否可以访问！", MFUpdateService.updatePendingIntent);
 
         MFUpdateService.this.updateNotificationManager.notify(MFUpdateService.iconID, MFUpdateService.this.updateNotification);
         break;
       case 2:
         MFUpdateService.this.updateNotification.setLatestEventInfo(MFUpdateService.this, MFUpdateService.this.updateNotificationTitle, "下载失败，请重新下载！", MFUpdateService.updatePendingIntent);
 
         MFUpdateService.this.updateNotificationManager.notify(MFUpdateService.iconID, MFUpdateService.this.updateNotification);
         break;
       }
 
       MFUpdateService.this.stopSelf();
       super.handleMessage(msg);
     }
   };
 
   private Runnable updateRunnable = new Runnable() {
     Message message = MFUpdateService.this.updateHandler.obtainMessage();
 
     public void run() {
       try { this.message.what = 0;
         if (!MFUpdateService.updateFile.exists())
           MFUpdateService.updateFile.createNewFile();
         else {
           try {
             PackageManager pManager = MFUpdateService.this.getPackageManager();
             PackageInfo pInfo = pManager.getPackageArchiveInfo(MFUpdateService.updateFile.getPath(), 1);
 
             System.out.println("pInfo :" + pInfo);
             System.out.println("pInfo versionCode:" + pInfo.versionCode);
 
             System.out.println("updateApkVersionCode :" + MFUpdateService.updateApkVersionCode);
 
             if (pInfo != null) {
               if ((MFUpdateService.updateApkVersionCode > 0) && (MFUpdateService.updateApkVersionCode == pInfo.versionCode)) {
                 MFUpdateService.this.updateHandler.sendMessage(this.message);
                 return;
               }
 
               MFUpdateService.updateFile.delete();
               MFUpdateService.updateFile.createNewFile();
             }
           }
           catch (Exception ex) {
             ex.printStackTrace();
           }
         }
 
         long size = 0L;
 
         if ((MFUpdateService.updateApkDownloadUrl == null) || ("".equals(MFUpdateService.updateApkDownloadUrl))) {
           this.message.what = 1;
         } else {
           size = MFUpdateService.this.downloadUpdateFile(MFUpdateService.updateApkDownloadUrl, MFUpdateService.updateFile, true);
           if (size == 0L) {
             this.message.what = 2;
           }
         }
         MFUpdateService.this.updateHandler.sendMessage(this.message);
       } catch (IOException e) {
         e.printStackTrace();
         this.message.what = 3;
         MFUpdateService.this.updateHandler.sendMessage(this.message);
       } catch (Exception ex) {
         ex.printStackTrace();
         this.message.what = 2;
         MFUpdateService.this.updateHandler.sendMessage(this.message);
       }
     }
   };
 
   public IBinder onBind(Intent intent)
   {
     return null;
   }
 
   public void onCreate()
   {
     layoutID = MFUpdateUtils.getIdByReflection(getApplicationContext(), "layout", "mofang_update_notification");
     textViewID = MFUpdateUtils.getIdByReflection(getApplicationContext(), "id", "mfupdate_notification_progresstext");
     blockViewID = MFUpdateUtils.getIdByReflection(getApplicationContext(), "id", "mfupdate_notification_progressblock");
     processbarViewID = MFUpdateUtils.getIdByReflection(getApplicationContext(), "id", "mfupdate_notification_progressbar");
     notificationBarIconViewID = MFUpdateUtils.getIdByReflection(getApplicationContext(), "id", "mfupdate_notification_icon");
     notificationBarAppNameViewID = MFUpdateUtils.getIdByReflection(getApplicationContext(), "id", "mfupdate_notification_appname");
     mfappInfo = MFUpdateUtils.getVersionInfo(this);
   }
 
   public int onStartCommand(Intent intent, int flags, int startId)
   {
     iconID = intent.getIntExtra("iconID", 0);
     appNameID = intent.getIntExtra("appNameID", 0);
     updateApkVersionCode = intent.getIntExtra("versionCode", 0);
     updateApkDownloadUrl = intent.getStringExtra("downloadUrl");
 
     createAPKStorageFile();
 
     this.updateNotificationTitle = getResources().getString(appNameID);
 
     this.updateNotificationManager = ((NotificationManager)getSystemService("notification"));
 
     this.updateNotification = new Notification();
 
     this.updateNotification.contentView = new RemoteViews(getApplication().getPackageName(), layoutID);
 
     Intent updateCompletingIntent = new Intent();
     updateCompletingIntent.setFlags(536870912);
     updateCompletingIntent.setClass(getApplication().getApplicationContext(), MFUpdateService.class);
 
     updatePendingIntent = PendingIntent.getActivity(this, appNameID, updateCompletingIntent, 268435456);
 
     this.updateNotification.icon = iconID;
     this.updateNotification.tickerText = "开始下载";
 
     this.updateNotification.contentIntent = updatePendingIntent;
     this.updateNotification.contentView.setImageViewResource(notificationBarIconViewID, iconID);
     this.updateNotification.contentView.setTextViewText(notificationBarAppNameViewID, this.updateNotificationTitle);
     this.updateNotification.contentView.setProgressBar(processbarViewID, 100, 0, false);
     this.updateNotification.contentView.setTextViewText(textViewID, "0% ");
     this.updateNotificationManager.cancel(iconID);
     this.updateNotificationManager.notify(iconID, this.updateNotification);
 
     new Thread(this.updateRunnable).start();
     return super.onStartCommand(intent, flags, startId);
   }
   class UpdateThread extends Thread{
	   Activity activity;
	   MFUpdateListener updateListener;
	   boolean showUpdateDialog;
	   boolean wifiOnly;
	   
	   UpdateThread( boolean wifiOnly, Activity activity, MFUpdateListener updateListener, boolean showUpdateDialog){
		   this.activity = activity;
		   this.updateListener = updateListener;
		   this.showUpdateDialog = showUpdateDialog;
		   this.wifiOnly = wifiOnly;
	   }
	   
	   public void run() {
         if ((this.wifiOnly) && (1 == MFNetworkUtils.getNetworkState(this.activity))) {
           MFUpdateAPKInfo mfInfo = MFUpdateUtils.checkNewVersion(MFUpdateService.CHECKURL);
           if ((null != mfInfo) && (mfInfo.getVersionCode() > MFUpdateService.mfappInfo.getVersionCode())) {
             if (this.showUpdateDialog)
               this.activity.runOnUiThread(new UpdateThread(activity, mfInfo, updateListener));
             else {
               this.updateListener.onDetectedNewVersion(this.activity, mfInfo.getVersionCode(), mfInfo.getVersionName(), mfInfo.getDescription(), mfInfo.getApkPath());
             }
 
           } else if ((null != mfInfo) && (mfInfo.getVersionCode() <= MFUpdateService.mfappInfo.getVersionCode()))
             this.updateListener.onDetectedNothing(this.activity);
           else {
             this.updateListener.onFailed(this.activity);
           }
         }
         else if ((this.wifiOnly) && (-1 == MFNetworkUtils.getWifiIsEnabled(this.activity))) {
           this.updateListener.onWifiOff(this.activity);
         }
         else if (!this.wifiOnly) {
           MFUpdateAPKInfo mfInfo = MFUpdateUtils.checkNewVersion(MFUpdateService.CHECKURL);
           if ((null != mfInfo) && (mfInfo.getVersionCode() > MFUpdateService.mfappInfo.getVersionCode())) {
             if (this.showUpdateDialog)
               this.activity.runOnUiThread(new MFUpdateService.UpdateThread(this.activity, mfInfo, this.updateListener));
             else {
               this.updateListener.onDetectedNewVersion(this.activity, mfInfo.getVersionCode(), mfInfo.getVersionName(), mfInfo.getDescription(), mfInfo.getApkPath());
             }
           }
           else if ((null != mfInfo) && (mfInfo.getVersionCode() <= MFUpdateService.mfappInfo.getVersionCode()))
             this.updateListener.onDetectedNothing(this.activity);
           else
             this.updateListener.onFailed(this.activity);
         }
       }
   }
   public static void check(Activity activity, MFUpdateListener updateListener, boolean showUpdateDialog, boolean wifiOnly)
   {
     mfappInfo = MFUpdateUtils.getVersionInfo(activity);
     CHECKURL = getCheckUrl(activity.getApplicationContext(), mfappInfo);
     new UpdateThread(wifiOnly, activity, updateListener, showUpdateDialog).start();
   }
 
   public static void check(Activity activity, MFUpdateListener updateListener, boolean showUpdateDialog)
   {
     check(activity, updateListener, showUpdateDialog, false);
   }
 
   public static void autoUpdate(Activity activity, int appNameID, int notificationIconId, boolean wifiOnly)
   {
     mfappInfo = MFUpdateUtils.getVersionInfo(activity.getApplicationContext());
     CHECKURL = getCheckUrl(activity.getApplicationContext(), mfappInfo);
     new Thread()
     {
       public void run() {
         if ((wifiOnly) && (1 == MFNetworkUtils.getNetworkState(activity))) {
           MFUpdateAPKInfo mfInfo = MFUpdateUtils.checkNewVersion(MFUpdateService.CHECKURL);
           if ((null != mfInfo) && (mfInfo.getVersionCode() > MFUpdateService.mfappInfo.getVersionCode())) {
             this.activity.runOnUiThread(new MFUpdateService.UpdateThread(this.activity, mfInfo, this.appNameID, this.notificationIconId));
           }

         } else if ((!this.wifiOnly) && (MFNetworkUtils.isNetworkAvailable(this.activity))) {
           MFUpdateAPKInfo mfInfo = MFUpdateUtils.checkNewVersion(MFUpdateService.CHECKURL);
           if ((null != mfInfo) && (mfInfo.getVersionCode() > MFUpdateService.mfappInfo.getVersionCode()))
             this.activity.runOnUiThread(new MFUpdateService.UpdateThread(this.activity, mfInfo, this.appNameID, this.notificationIconId));
         }
       }
     }
     .start();
   }
 
   public static void autoUpdate(Activity activity, int appNameID, int notificationIconId)
   {
     autoUpdate(activity, appNameID, notificationIconId, false);
   }
 
   private static String getCheckUrl(Context context, MFAPPInfo mfappInfo)
   {
     if (null == CHECKURL) {
       try {
         String mofangAppKey = (String)context.getPackageManager().getApplicationInfo(context.getPackageName(), 128).metaData.get("MOFANG_APPKEY");
 
         if ((null != mofangAppKey) && (!"".equals(mofangAppKey)))
           CHECKURL = "http://m.imofan.com/online_update/?app_key=" + mofangAppKey + "&ver=" + mfappInfo.getVersionName();
       }
       catch (PackageManager.NameNotFoundException e) {
         e.printStackTrace();
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
     return CHECKURL;
   }
 
   private long downloadUpdateFile(String urlStr, File dest, boolean append)
     throws Exception
   {
     int downloadCount = 0;
     int currentSize = 0;
     long totalSize = 0L;
     if (append) {
       FileInputStream fis = null;
       try {
         fis = new FileInputStream(dest);
         currentSize = fis.available();
       } catch (IOException ex) {
         ex.printStackTrace();
       } finally {
         if (fis != null) {
           fis.close();
         }
       }
     }
     HttpURLConnection httpConnection = null;
     InputStream is = null;
     FileOutputStream fos = null;
     try {
       URL url = new URL(urlStr);
       httpConnection = (HttpURLConnection)url.openConnection();
       httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
       if (currentSize > 0) {
         httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
       }
       httpConnection.setConnectTimeout(10000);
       httpConnection.setReadTimeout(20000);
       updateTotalSize = httpConnection.getContentLength();
       if (httpConnection.getResponseCode() == 404) {
         throw new Exception("Cannot find remote file:" + urlStr);
       }
       is = httpConnection.getInputStream();
       fos = new FileOutputStream(dest, append);
       byte[] buffer = new byte[4096];
       int readsize = 0;
       while ((readsize = is.read(buffer)) > 0) {
         fos.write(buffer, 0, readsize);
         totalSize += readsize;
         if ((downloadCount == 0) || ((int)(totalSize * 100L / updateTotalSize) - 8 > downloadCount)) {
           downloadCount += 8;
           this.updateNotification.contentView.setProgressBar(processbarViewID, 100, (int)(totalSize * 100L / updateTotalSize), false);
 
           this.updateNotification.contentView.setTextViewText(textViewID, (int)(totalSize * 100L / updateTotalSize) + "%");
 
           this.updateNotificationManager.notify(iconID, this.updateNotification);
         }
       }
     } finally {
       if (httpConnection != null) {
         httpConnection.disconnect();
       }
       if (is != null) {
         is.close();
       }
       if (fos != null) {
         fos.close();
       }
     }
     return totalSize;
   }
 
   private void createAPKStorageFile()
   {
     internalFileDir = getDir("update", 0);
     if ((null != internalFileDir) && (internalFileDir.isDirectory())) {
       File[] files = internalFileDir.listFiles();
       if (null != files) {
         for (int i = 0; i < files.length; i++) {
           MFUpdateUtils.delete(files[i]);
         }
       }
     }
     updateFile = new File(internalFileDir.getAbsolutePath(), File.separator + mfappInfo.getPackageName() + "_" + updateApkVersionCode + ".apk");
   }
 
   private static class UpdateThread
     implements Runnable
   {
     private Activity activity;
     private MFUpdateAPKInfo updateInfo;
     private int notificationIconResId;
     private int appNameID;
     private MFUpdateListener mfUpdateListener;
 
     public UpdateThread(Activity activity, MFUpdateAPKInfo mfUpdateInfo, int appNameID, int notificationIconResId)
     {
       this.activity = activity;
       this.updateInfo = mfUpdateInfo;
       this.appNameID = appNameID;
       this.notificationIconResId = notificationIconResId;
     }
 
     public UpdateThread(Activity activity, MFUpdateAPKInfo mfUpdateInfo, MFUpdateListener listener) {
       this.activity = activity;
       this.updateInfo = mfUpdateInfo;
       this.mfUpdateListener = listener;
     }
 
     public void run() {
       AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.activity);
       alertBuilder.setTitle("软件升级").setPositiveButton("更新", new DialogInterface.OnClickListener()
       {
         public void onClick(DialogInterface dialog, int id) {
           if (MFUpdateService.UpdateThread.this.mfUpdateListener != null) {
             MFUpdateService.UpdateThread.this.mfUpdateListener.onPerformUpdate(MFUpdateService.UpdateThread.this.activity, MFUpdateService.UpdateThread.this.updateInfo.getVersionCode(), MFUpdateService.UpdateThread.this.updateInfo.getVersionName(), MFUpdateService.UpdateThread.this.updateInfo.getDescription(), MFUpdateService.UpdateThread.this.updateInfo.getApkPath());
           }
           else
           {
             Intent serviceIntent = new Intent(MFUpdateService.UpdateThread.this.activity, MFUpdateService.class);
             serviceIntent.putExtra("iconID", MFUpdateService.UpdateThread.this.notificationIconResId);
             serviceIntent.putExtra("appNameID", MFUpdateService.UpdateThread.this.appNameID);
             serviceIntent.putExtra("downloadUrl", MFUpdateService.UpdateThread.this.updateInfo.getApkPath());
             serviceIntent.putExtra("versionCode", MFUpdateService.UpdateThread.this.updateInfo.getVersionCode());
             MFUpdateService.UpdateThread.this.activity.startService(serviceIntent);
           }
         }
       }).setNegativeButton("取消", new DialogInterface.OnClickListener()
       {
         public void onClick(DialogInterface dialog, int id)
         {
           dialog.cancel();
         }
       }).setView(MFUpdateUtils.createUpdateDialog(this.activity, this.updateInfo));
 
       alertBuilder.create().show();
     }
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.update.MFUpdateService
 * JD-Core Version:    0.6.0
 */
