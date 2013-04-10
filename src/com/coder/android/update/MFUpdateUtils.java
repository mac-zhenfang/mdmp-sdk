 package com.coder.android.update;
 
 import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
 
 public class MFUpdateUtils
 {
   protected static MFAPPInfo getVersionInfo(Context context)
   {
     MFAPPInfo mfappInfo = null;
     try {
       PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 16384);
 
       mfappInfo = new MFAPPInfo();
       mfappInfo.setPackageName(packageInfo.packageName);
       mfappInfo.setVersionCode(packageInfo.versionCode);
       mfappInfo.setVersionName(packageInfo.versionName);
     } catch (PackageManager.NameNotFoundException e) {
       e.printStackTrace();
     }
     return mfappInfo;
   }
 
   protected static MFUpdateAPKInfo checkNewVersion(String checkUrl)
   {
     MFUpdateAPKInfo mfInfo = null;
     try {
       String json = MFHttpUtils.invokeText(checkUrl);
       if ((json != null) && (json.trim().length() > 0)) {
         mfInfo = new MFUpdateAPKInfo();
         JSONObject jsonObject = new JSONObject(json);
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         String time = jsonObject.optString("time");
         if ((time != null) && (!"".equals(time))) {
           mfInfo.setTime(dateFormat.parse(jsonObject.optString("time")));
         }
         mfInfo.setVersionCode(jsonObject.getInt("versionCode"));
         mfInfo.setVersionName(jsonObject.getString("versionName"));
         mfInfo.setDescription(jsonObject.getString("description"));
         mfInfo.setApkPath(jsonObject.getString("url"));
       }
     } catch (Exception e) {
       mfInfo = null;
       e.printStackTrace();
     }
     return mfInfo;
   }
 
   protected static int getIdByReflection(Context context, String className, String fieldName)
   {
     int id = 0;
     try
     {
       Class idClass = Class.forName(context.getPackageName() + ".R$" + className);
 
       Field idField = idClass.getField(fieldName);
       id = idField.getInt(fieldName);
     } catch (ClassNotFoundException e) {
       e.printStackTrace();
     } catch (SecurityException e) {
       e.printStackTrace();
     } catch (IllegalArgumentException e) {
       e.printStackTrace();
     } catch (IllegalAccessException e) {
       e.printStackTrace();
     } catch (NoSuchFieldException e) {
       e.printStackTrace();
     }
     return id;
   }
 
   protected static View createUpdateDialog(Activity activity, MFUpdateAPKInfo updateInfo)
   {
     LinearLayout linearLayout = new LinearLayout(activity);
     linearLayout.setOrientation(1);
     linearLayout.setPadding(12, 5, 12, 0);
     LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(-1, -1);
 
     TextView textView = new TextView(activity);
     textView.setPadding(5, 0, 5, 0);
     textView.setText(replaceBr(updateInfo.getDescription()));
     textView.setTextSize(2, 18.0F);
     textView.setTextColor(-1);
 
     linearLayout.addView(textView, param);
 
     return linearLayout;
   }
 
   public static String replaceBr(String str)
   {
     if ((str == null) || ("".equals(str))) {
       return "";
     }
     return str.replaceAll("(?i)<br>", "\n").replace("，", ",");
   }
 
   protected static boolean deleteDir(File dir)
   {
     if ((null != dir) && (dir.isDirectory())) {
       String[] children = dir.list();
       for (int i = 0; i < children.length; i++) {
         boolean success = deleteDir(new File(dir, children[i]));
         if (!success) {
           return false;
         }
       }
     }
     return dir.delete();
   }
 
   protected static boolean delete(File file)
   {
     if (!file.exists()) {
       return false;
     }
     if (file.isFile()) {
       return deleteFile(file);
     }
     return deleteDirectory(file, true);
   }
 
   private static boolean deleteDirectory(File dirFile, boolean includeSelf)
   {
     return deleteDirectory(dirFile, null, includeSelf, false);
   }
 
   private static boolean deleteDirectory(File dirFile, String extension, boolean includeSelf, boolean onlyFile)
   {
     if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
       return false;
     }
     boolean flag = true;
     File[] files = dirFile.listFiles();
     for (int i = 0; i < files.length; i++) {
       if (files[i].isFile()) {
         if ((extension != null) && (!files[i].getName().toLowerCase().endsWith("." + extension.toLowerCase())))
           continue;
         flag = deleteFile(files[i]);
         if (!flag) {
           break;
         }
 
       }
       else if (!onlyFile) {
         flag = deleteDirectory(files[i], true);
         if (!flag)
         {
           break;
         }
       }
     }
 
     if (!flag) {
       return false;
     }
 
     if (includeSelf)
     {
       return dirFile.delete();
     }
 
     return true;
   }
 
   private static boolean deleteFile(File file)
   {
     if ((file.isFile()) && (file.exists())) {
       file.delete();
       return true;
     }
     return false;
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.update.MFUpdateUtils
 * JD-Core Version:    0.6.0
 */
