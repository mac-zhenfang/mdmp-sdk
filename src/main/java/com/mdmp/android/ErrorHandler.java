 package com.mdmp.android;
 
 import android.os.Process;
 import android.util.Log;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.json.JSONException;
import org.json.JSONObject;

import com.mdmp.android.db.DatabaseManager;
import com.mdmp.android.event.StatEvent;
 
 class ErrorHandler
   implements Thread.UncaughtExceptionHandler
 {
   private static final String LOG_TAG = "Mofang_MFCrashHandler";
   private static ErrorHandler self;
   private DatabaseManager dbManager;
 
   private ErrorHandler(DatabaseManager dbManager)
   {
     this.dbManager = dbManager;
   }
 
   static synchronized void init(DatabaseManager dbManager) {
     if (self == null) {
       self = new ErrorHandler(dbManager);
       Thread.setDefaultUncaughtExceptionHandler(self);
     }
   }
 
//   @Override
   public void uncaughtException(Thread thread, Throwable throwable) {
     Log.e("Mofang_MFCrashHandler", "[uncaughtException]Find uncaught exception");
     throwable.printStackTrace();
     JSONObject exceptionJson = new JSONObject();
     try {
       exceptionJson.put("name", throwable.toString());
       exceptionJson.put("stack", Log.getStackTraceString(throwable));
 
       Date now = new Date();
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
       SimpleDateFormat hourFormat = new SimpleDateFormat("H");
       StatEvent.addEvent(this.dbManager, "crash", Integer.parseInt(dateFormat.format(now)), Integer.parseInt(hourFormat.format(now)), exceptionJson.toString());
     }
     catch (JSONException e)
     {
     }
     Process.killProcess(Process.myPid());
   }
 }
