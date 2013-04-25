 package com.mdmp.android.update;
 
 import java.util.Date;
 
 public class MFUpdateAPKInfo
 {
   private int versionCode;
   private String versionName;
   private String description;
   private String apkPath;
   private Date time;
 
   public int getVersionCode()
   {
     return this.versionCode;
   }
 
   public void setVersionCode(int versionCode)
   {
     this.versionCode = versionCode;
   }
 
   public String getVersionName()
   {
     return this.versionName;
   }
 
   public void setVersionName(String versionName)
   {
     this.versionName = versionName;
   }
 
   public String getDescription()
   {
     return this.description;
   }
 
   public void setDescription(String description)
   {
     this.description = description;
   }
 
   public String getApkPath()
   {
     return this.apkPath;
   }
 
   public void setApkPath(String apkPath)
   {
     this.apkPath = apkPath;
   }
 
   public Date getTime()
   {
     return this.time;
   }
 
   public void setTime(Date time)
   {
     this.time = time;
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.update.MFUpdateAPKInfo
 * JD-Core Version:    0.6.0
 */
