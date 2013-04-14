 package com.mdmp.android.log;
 
 public class Log
 {
   public static boolean LOG = false;
 
   public static void a(String paramString1, String paramString2) {
     if (LOG)
       android.util.Log.i(paramString1, paramString2);
   }
 
   public static void a(String paramString1, String paramString2, Exception paramException)
   {
     if (LOG)
       android.util.Log.i(paramString1, paramException.toString() + ":  [" + paramString2 + "]");
   }
 
   public static void b(String paramString1, String paramString2)
   {
     if (LOG)
       android.util.Log.e(paramString1, paramString2);
   }
 
   public static void b(String paramString1, String paramString2, Exception paramException)
   {
     if (LOG) {
       android.util.Log.e(paramString1, paramException.toString() + ":  [" + paramString2 + "]");
       StackTraceElement[] arrayOfStackTraceElement1 = paramException.getStackTrace();
       for (StackTraceElement localStackTraceElement : arrayOfStackTraceElement1)
         android.util.Log.e(paramString1, "        at\t " + localStackTraceElement.toString());
     }
   }
 
   public static void c(String paramString1, String paramString2) {
     if (LOG)
       android.util.Log.d(paramString1, paramString2);
   }
 
   public static void c(String paramString1, String paramString2, Exception paramException)
   {
     if (LOG)
       android.util.Log.d(paramString1, paramException.toString() + ":  [" + paramString2 + "]");
   }
 
   public static void d(String paramString1, String paramString2)
   {
     if (LOG)
       android.util.Log.v(paramString1, paramString2);
   }
 
   public static void d(String paramString1, String paramString2, Exception paramException)
   {
     if (LOG)
       android.util.Log.v(paramString1, paramException.toString() + ":  [" + paramString2 + "]");
   }
 
   public static void e(String paramString1, String paramString2)
   {
     if (LOG)
       android.util.Log.w(paramString1, paramString2);
   }
 
   public static void e(String paramString1, String paramString2, Exception paramException)
   {
     if (LOG) {
       android.util.Log.w(paramString1, paramException.toString() + ":  [" + paramString2 + "]");
       StackTraceElement[] arrayOfStackTraceElement1 = paramException.getStackTrace();
       for (StackTraceElement localStackTraceElement : arrayOfStackTraceElement1)
         android.util.Log.w(paramString1, "        at\t " + localStackTraceElement.toString());
     }
   }
 }
