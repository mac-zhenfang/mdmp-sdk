 package com.coder.android.notification;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import java.util.Date;
 
 public class MFAlarmReceiver extends BroadcastReceiver
 {
   public void onReceive(Context context, Intent intent)
   {
     boolean pushStatus = PreferencesUtils.getPreference(context, "push", "status", false);
     if (pushStatus)
     {
       int time = new Date(System.currentTimeMillis()).getHours();
       if ((time > 23) || (time < 7)) {
         return;
       }
 
       if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
         String listenerName = PreferencesUtils.getPreference(context, "mfpush", "pushListener", "");
         if ((null != listenerName) && (!"".equals(listenerName))) {
           try {
             MFPNService.startPushService(context, Class.forName(listenerName));
           } catch (ClassNotFoundException e) {
             e.printStackTrace();
           }
         }
 
       }
 
       Intent messageIntent = new Intent(context, MFPNService.class);
       context.startService(messageIntent);
     }
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.notification.MFAlarmReceiver
 * JD-Core Version:    0.6.0
 */
