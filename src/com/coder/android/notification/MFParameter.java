 package com.coder.android.notification;
 
 import android.content.Context;
 
 public class MFParameter
 {
   private long appId;
   private String devId;
   private long md5;
   private long timeStamp = 0L;
 
   public long getAppId()
   {
     return this.appId;
   }
 
   public void setAppId(long appId) {
     this.appId = appId;
   }
 
   public String getDevId() {
     return this.devId;
   }
 
   public void setDevId(String devId) {
     this.devId = devId;
   }
 
   public long getMd5() {
     return this.md5;
   }
 
   public void setMd5(long md5) {
     this.md5 = md5;
   }
 
   public long getTimeStamp() {
     return this.timeStamp;
   }
 
   public void setTimeStamp(long timeStamp) {
     this.timeStamp = timeStamp;
   }
 
   public static long getMd5Verify(Context context, long appId, long timeStamp)
   {
     long md5 = timeStamp % appId;
     return md5;
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.notification.MFParameter
 * JD-Core Version:    0.6.0
 */
