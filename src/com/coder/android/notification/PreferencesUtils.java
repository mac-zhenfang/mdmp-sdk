 package com.coder.android.notification;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 public class PreferencesUtils
 {
   public static void setPreferences(Context context, String preference, String key, boolean value)
   {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     SharedPreferences.Editor editor = sharedPreferences.edit();
     editor.putBoolean(key, value);
     editor.commit();
   }
 
   public static void setPreferences(Context context, String preference, String key, long value) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     SharedPreferences.Editor editor = sharedPreferences.edit();
     editor.putLong(key, value);
     editor.commit();
   }
 
   public static void setPreferences(Context context, String preference, String key, int value) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     SharedPreferences.Editor editor = sharedPreferences.edit();
     editor.putInt(key, value);
     editor.commit();
   }
 
   public static void setPreferences(Context context, String preference, String key, String value) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     SharedPreferences.Editor editor = sharedPreferences.edit();
     editor.putString(key, value);
     editor.commit();
   }
 
   public static String getPreference(Context context, String preference, String key, String defaultValue) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     return sharedPreferences.getString(key, defaultValue);
   }
 
   public static boolean getPreference(Context context, String preference, String key, boolean defaultValue) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     return sharedPreferences.getBoolean(key, defaultValue);
   }
 
   public static long getPreference(Context context, String preference, String key, long defaultValue) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     return sharedPreferences.getLong(key, defaultValue);
   }
 
   public static int getPreference(Context context, String preference, String key, int defaultValue) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
     return sharedPreferences.getInt(key, defaultValue);
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.notification.PreferencesUtils
 * JD-Core Version:    0.6.0
 */
