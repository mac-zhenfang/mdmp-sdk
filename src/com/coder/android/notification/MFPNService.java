 package com.coder.android.notification;
 
 import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.coder.android.DataAgent;
 
 public class MFPNService extends Service
 {
   private static final String TAG = MFPNService.class.getSimpleName();
   private static final long intervalAlarmTime = 300000L;
   private static PowerManager.WakeLock wakeLock;
   private final int MESSAGE_PUSH_SUCCEED = 1;
   private static PendingIntent pendingIntent = null;
   private long messageLatestTime;
   private Handler messagePushHandler;
 
   public MFPNService()
   {
 
     this.messagePushHandler = new Handler()
     {
       public void handleMessage(Message msg) {
         switch (msg.what)
         {
         case 1:
           MFMessage mfMessage = (MFMessage)msg.obj;
 
           if (null == mfMessage)
             break;
           MFNotificationListener mfNotificationListener = MFPushUtils.getNotificationListener(MFPNService.this.getApplicationContext());
           if (null != mfNotificationListener) {
             mfNotificationListener.onNotificationReceived(MFPNService.this.getApplicationContext(), String.valueOf(mfMessage.getMsgId()), mfMessage.getMsgContent());
           }
 
           break;
         }
         stopSelf();
       } } ;
   }
 
   public IBinder onBind(Intent intent) {
     return null;
   }
 
   public int onStartCommand(Intent intent, int flags, int startId)
   {
     if (intent == null)
     {
       try {
         Intent broadCastIntent = new Intent("com.imofan.android.basic.notification.MFAlarmReceiver");
         sendBroadcast(broadCastIntent);
       } catch (Exception e) {
         e.printStackTrace();
       }
       return super.onStartCommand(intent, flags, startId);
     }
 
     acquireWakeLock(this);
 
     this.messageLatestTime = PreferencesUtils.getPreference(this, "message", "latestTime", 0L);
 
     Thread messageThread = new Thread(new MessagePushRunnable());
     messageThread.start();
     return super.onStartCommand(intent, flags, startId);
   }
 
   private String getPushContent()
   {
     String message = null;
     MFParameter mfParameter = new MFParameter();
 
     String appKey = DataAgent.getAppKey(getApplicationContext());
     Log.v(TAG, "AppKey: " + appKey);
     String devID = MFPushUtils.getDevId(getApplicationContext());
     Log.v(TAG, "DevId: " + devID);
 
     if ((null == appKey) || (null == devID)) {
       releaseWakeLock();
       return null;
     }
     long appId = Long.valueOf(appKey.substring(appKey.length() - 8), 16).longValue();
     Log.v(TAG, "AppId: " + appId);
     mfParameter.setDevId(devID);
     mfParameter.setAppId(appId);
     Log.v(TAG, "LatestTime：" + this.messageLatestTime);
     mfParameter.setTimeStamp(this.messageLatestTime);
     Log.v(TAG, "MD5: " + MFParameter.getMd5Verify(getApplicationContext(), appId, this.messageLatestTime));
     mfParameter.setMd5(MFParameter.getMd5Verify(getApplicationContext(), appId, this.messageLatestTime));
 
     message = MFPushUtils.pullMsgByPost(mfParameter);
     return message;
   }
 
   public static void startPushService(Context context, Class listenerClass)
   {
     String devId = MFPushUtils.getDevId(context);
     if (null != listenerClass)
     {
       MFPushUtils.saveListenerCLass(context, listenerClass);
     }
 
     if ((null != devId) && (!"".equals(devId))) {
       AlarmManager alarmManager = (AlarmManager)context.getSystemService("alarm");
       Intent intent = new Intent(context, MFAlarmReceiver.class);
       if (null != pendingIntent)
       {
         alarmManager.cancel(pendingIntent);
       }
       pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 268435456);
 
       alarmManager.setRepeating(0, System.currentTimeMillis(), 300000L, pendingIntent);
     }
   }
 
   public static void stopPushService(Context context)
   {
     AlarmManager alarmManager = (AlarmManager)context.getSystemService("alarm");
     if (null != pendingIntent) {
       alarmManager.cancel(pendingIntent);
       pendingIntent = null;
     }
   }
 
   public static void setPushStatus(Context context, boolean status)
   {
     PreferencesUtils.setPreferences(context, "push", "status", status);
   }
 
   private static void acquireWakeLock(Context context)
   {
     if (wakeLock != null)
     {
       return;
     }
     PowerManager powerManager = (PowerManager)(PowerManager)context.getSystemService("power");
     wakeLock = powerManager.newWakeLock(1, TAG);
     wakeLock.acquire();
   }
 
   private static void releaseWakeLock()
   {
     if ((wakeLock != null) && (wakeLock.isHeld())) {
       wakeLock.release();
       wakeLock = null;
     }
   }
 
   public void onDestroy()
   {
     super.onDestroy();
     releaseWakeLock();
   }
 
   private class MessagePushRunnable
     implements Runnable
   {
     private MessagePushRunnable()
     {
     }
 
     public void run()
     {
       Message msg = null;
 
       MFMessage mfMessage = null;
 
       String jsonContent = MFPNService.this.getPushContent();
 
       List<MFMessage> msgs = MFPushUtils.parseJson(jsonContent);
 
       mfMessage = MFPushUtils.getMaxTsMsg(MFPNService.this.getApplicationContext(), msgs);
       if (null != mfMessage) {
         msg = MFPNService.this.messagePushHandler.obtainMessage();
         msg.obj = mfMessage;
         msg.what = 1;
         MFPNService.this.messagePushHandler.sendMessage(msg);
       } else {
    	   stopSelf();
       }
     }
   }
 }
