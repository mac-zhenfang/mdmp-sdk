 package com.coder.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 class AccessPath
 {
   private static final String LOG_TAG = "Mofang_MFStatAccessPath";
 
   static void addAccessNode(int id, String node, long duration)
   {
     String accessPath = StatInfo.getString("user_activities", null);
     if ((accessPath == null) || (accessPath.trim().length() == 0))
       accessPath = "";
     else {
       accessPath = accessPath + ";";
     }
     if ((node != null) && (node.trim().length() > 0)) {
       node = node.trim().replaceAll(",", "\\co\\").replaceAll(";", "\\se\\").replaceAll("Exit", "\\Exit\\");
     }
     else {
       node = "Exit";
     }
     accessPath = accessPath + id + "," + node + "," + duration;
     StatInfo.putString("user_activities", accessPath);
   }
 
   static void addAccessNode(int id, String node)
   {
     addAccessNode(id, node, 0L);
   }
 
   static void updateAccessDuration(int id, long duration)
   {
     String accessPath = StatInfo.getString("user_activities", null);
     if ((accessPath == null) || (accessPath.trim().length() == 0)) {
       return;
     }
 
     String str2 = ";" + id + ",";
     String[] str1And3456 = accessPath.split(str2);
     if (str1And3456.length == 1) {
       str2 = id + ",";
       str1And3456 = new String[2];
       str1And3456[0] = "";
       str1And3456[1] = accessPath.substring(accessPath.indexOf(",") + 1);
     }
     String str1 = str1And3456[0];
     String str3 = str1And3456[1].substring(0, str1And3456[1].indexOf(","));
     String str4 = ",";
     String str56 = str1And3456[1].substring(str1And3456[1].indexOf(",") + 1);
     String str5 = Long.toString(duration);
     String str6 = "";
     if (str56.indexOf(";") > 0) {
       str6 = str56.substring(str56.indexOf(";"));
     }
     accessPath = str1 + str2 + str3 + str4 + str5 + str6;
     StatInfo.putString("user_activities", accessPath);
   }
 
   static List<Node> getAccessNodes()
   {
     List accessNodes = null;
     String accessPath = StatInfo.getString("user_activities", null);
     if ((accessPath != null) && (accessPath.trim().length() > 0)) {
       accessNodes = new ArrayList();
       String[] paths = accessPath.split(";");
       for (String path : paths) {
         if (path.trim().length() > 0) {
           Node accessNode = new Node();
           String[] info = path.trim().split(",");
           if (!info[1].equals("Exit")) {
             info[1] = info[1].replaceAll("\\\\co\\\\", ",").replaceAll("\\\\se\\\\", ";").replaceAll("\\\\Exit\\\\", "Exit");
 
             accessNode.setActivity(info[1]);
           }
           accessNode.setDuration(Long.parseLong(info[2]));
           accessNodes.add(accessNode);
         }
       }
     }
     return accessNodes;
   }
   static class Node { private static final String EXIT_NODE = "Exit";
     private String activity;
     private int count;
     private int exit;
     private long duration;
 
     public String getActivity() { return this.activity; }
 
     public void setActivity(String activity)
     {
       this.activity = activity;
     }
 
     public void incCount() {
       this.count += 1;
     }
 
     public int getCount() {
       return this.count;
     }
 
     public void setCount(int count) {
       this.count = count;
     }
 
     public void incExit() {
       this.exit += 1;
     }
 
     public int getExit() {
       return this.exit;
     }
 
     public void setExit(int exit) {
       this.exit = exit;
     }
 
     public long getDuration() {
       return this.duration;
     }
 
     public void setDuration(long duration) {
       this.duration = duration;
     }
   }
 }
