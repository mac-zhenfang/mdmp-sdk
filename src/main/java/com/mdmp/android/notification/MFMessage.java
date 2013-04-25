 package com.mdmp.android.notification;
 
 public class MFMessage
 {
   private int msgId;
   private String appId;
   private String appVersion;
   private String osVersion;
   private String msgContent;
   private long timeStamp;
   private String language;
 
   public int getMsgId()
   {
     return this.msgId;
   }
   public void setMsgId(int msgId) {
     this.msgId = msgId;
   }
   public String getAppId() {
     return this.appId;
   }
   public void setAppId(String appId) {
     this.appId = appId;
   }
   public String getAppVersion() {
     return this.appVersion;
   }
   public void setAppVersion(String appVersion) {
     this.appVersion = appVersion;
   }
   public String getOsVersion() {
     return this.osVersion;
   }
   public void setOsVersion(String osVersion) {
     this.osVersion = osVersion;
   }
   public String getMsgContent() {
     return this.msgContent;
   }
   public void setMsgContent(String msgContent) {
     this.msgContent = msgContent;
   }
   public long getTimeStamp() {
     return this.timeStamp;
   }
   public void setTimeStamp(long timeStamp) {
     this.timeStamp = timeStamp;
   }
   public String getLanguage() {
     return this.language;
   }
   public void setLanguage(String language) {
     this.language = language;
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.notification.MFMessage
 * JD-Core Version:    0.6.0
 */
