 package com.coder.android.event;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import java.util.ArrayList;
 import java.util.Iterator;
import java.util.List;

import com.coder.android.db.DatabaseManager;
 
 public class StatEvent
 {
   private static final String LOG_TAG = "Mofang_MFEvent";
   static final String TABLE = "event";
   static final String FIELD_ID = "id";
   static final String FIELD_DATE = "date";
   static final String FIELD_HOUR = "hour";
   static final String FIELD_EVENT = "event";
   static final String FIELD_VALUE = "value";
   static final String FIELD_COUNT = "count";
   static final String FIELD_EXIT = "exit";
   static final String FIELD_DURATION = "duration";
   static final String FIELD_FIRST = "first";
   static final String FIELD_TIME = "time";
   static final String FIELD_SEND = "send";
   static final String EVENT_INSTALL = "install";
   static final String EVENT_UPGRADE = "upgrade";
   static final String EVENT_OPEN = "open";
   static final String EVENT_COLSE = "close";
   static final String EVENT_RETURN_USER = "return";
   static final String EVENT_DEVELOPER = "developer";
   static final String EVENT_UPDATE = "update";
   static final String EVENT_ACCESS_PATH = "path";
   static final String EVENT_ACCESS_NODE = "node";
   public static final String EVENT_NOTIFICATION_RECEIVE = "notification_receive";
   public static final String EVENT_NOTIFICATION_OPEN = "notification_open";
   static final String EVENT_CRASH = "crash";
   static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS event (id INTEGER PRIMARY KEY, date INTEGER NOT NULL, hour INTEGER NOT NULL, event TEXT NOT NULL, value TEXT, count INTEGER DEFAULT 1, exit INTEGER DEFAULT 0, duration INTEGER DEFAULT 0, first INTEGER DEFAULT 0, time NUMERIC NOT NULL, send INTEGER DEFAULT 0);";
   private int id;
   private int date;
   private int hour;
   private String event;
   private String value;
   private int count = 0;
   private int exit = 0;
   private long duration = 0L;
   private boolean first = false;
   private long time = System.currentTimeMillis();
   private boolean send = false;
 
   public static StatEvent parse(Cursor cur)
   {
     StatEvent event = new StatEvent();
     if (cur.getColumnIndex("id") > -1) {
       event.setId(cur.getInt(cur.getColumnIndex("id")));
     }
     if (cur.getColumnIndex("date") > -1) {
       event.setDate(cur.getInt(cur.getColumnIndex("date")));
     }
     if (cur.getColumnIndex("hour") > -1) {
       event.setHour(cur.getInt(cur.getColumnIndex("hour")));
     }
     if (cur.getColumnIndex("event") > -1) {
       event.setEvent(cur.getString(cur.getColumnIndex("event")));
     }
     if (cur.getColumnIndex("value") > -1) {
       event.setValue(cur.getString(cur.getColumnIndex("value")));
     }
     if (cur.getColumnIndex("count") > -1) {
       event.setCount(cur.getInt(cur.getColumnIndex("count")));
     }
     if (cur.getColumnIndex("exit") > -1) {
       event.setExit(cur.getInt(cur.getColumnIndex("exit")));
     }
     if (cur.getColumnIndex("duration") > -1) {
       event.setDuration(cur.getLong(cur.getColumnIndex("duration")));
     }
     if (cur.getColumnIndex("first") > -1) {
       event.setFirst(cur.getInt(cur.getColumnIndex("first")) == 1);
     }
     if (cur.getColumnIndex("time") > -1) {
       event.setTime(cur.getLong(cur.getColumnIndex("time")));
     }
     if (cur.getColumnIndex("send") > -1) {
       event.setSend(cur.getInt(cur.getColumnIndex("send")) == 1);
     }
     return event;
   }
 
   private void persist(DatabaseManager dbManager)
   {
     SQLiteDatabase database = dbManager.getWritableDatabase();
     ContentValues cv = new ContentValues();
     cv.put("date", Integer.valueOf(getDate()));
     cv.put("hour", Integer.valueOf(getHour()));
     cv.put("event", getEvent());
     if (getValue() != null) {
       cv.put("value", getValue());
     }
     cv.put("exit", Integer.valueOf(getExit()));
     cv.put("count", Integer.valueOf(getCount()));
     cv.put("duration", Long.valueOf(getDuration()));
     cv.put("first", Integer.valueOf(isFirst() ? 1 : 0));
     cv.put("time", Long.valueOf(getTime()));
     cv.put("send", Integer.valueOf(isSend() ? 1 : 0));
     database.insert("event", null, cv);
   }
 
   public static List<StatEvent> getEvents(DatabaseManager dbManager, String event, String value, int date, boolean send)
   {
     String where = " where send=?";
     List paramList = new ArrayList();
     if (send)
       paramList.add("1");
     else {
       paramList.add("0");
     }
     if ((event != null) && (event.trim().length() > 0)) {
       where = where + " and event=?";
       paramList.add(event);
     }
     if ((value != null) && (value.trim().length() > 0)) {
       if (where.length() > 0)
         where = where + " and value=?";
       else {
         where = "value=?";
       }
       paramList.add(value);
     }
     if (date > 0) {
       if (where.length() > 0)
         where = where + " and date=?";
       else {
         where = "date=?";
       }
       paramList.add(Integer.toString(date));
     }
     String[] params = null;
     params = new String[paramList.size()];
     for (int i = 0; i < paramList.size(); i++) {
       params[i] = ((String)paramList.get(i));
     }
 
     SQLiteDatabase db = dbManager.getReadableDatabase();
     Cursor cur = null;
     try {
       cur = db.rawQuery("select * from event" + where + " order by " + "time", params);
 
       if (cur.getCount() > 0) {
         List events = new ArrayList(cur.getCount());
         while (cur.moveToNext()) {
           events.add(parse(cur));
         }
         List localList1 = events;
         return localList1;
       }
     }
     finally
     {
       if (cur != null) {
         cur.close();
       }
     }
     return null;
   }
 
   public static int[] getEventDates(DatabaseManager dbManager) {
     int[] dates = null;
     SQLiteDatabase db = dbManager.getReadableDatabase();
     Cursor cur = null;
     try {
       cur = db.rawQuery("select date from event where send=? and event<>? order by date", new String[] { "0", "update" });
 
       if (cur.getCount() > 0) {
         List dateList = new ArrayList();
         int lastDate = 0;
         while (cur.moveToNext()) {
           int date = cur.getInt(cur.getColumnIndex("date"));
           if ((date > 0) && (date != lastDate)) {
             dateList.add(Integer.valueOf(date));
             lastDate = date;
           }
         }
         if (dateList.size() > 0) {
           dates = new int[dateList.size()];
           for (int i = 0; i < dateList.size(); i++)
             dates[i] = ((Integer)dateList.get(i)).intValue();
         }
       }
     }
     finally {
       if (cur != null) {
         cur.close();
       }
     }
     return dates;
   }
 
   public static void addEvent(DatabaseManager dbManager, String event, int date, int hour, String value, int count, int exit, long duration, boolean first, long time)
   {
     StatEvent ev = new StatEvent();
     ev.setEvent(event);
     ev.setDate(date);
     ev.setHour(hour);
     ev.setValue(value);
     ev.setCount(count);
     ev.setExit(exit);
     ev.setDuration(duration);
     ev.setFirst(first);
     if (time > 0L) {
       ev.setTime(time);
     }
     try
     {
       ev.persist(dbManager);
     }
     catch (Exception e)
     {
       e.printStackTrace();
     }
   }
 
   public static void clearAccessPath(DatabaseManager dbManager)
   {
     SQLiteDatabase database = dbManager.getWritableDatabase();
     database.delete("event", "event=?", new String[] { "path" });
     database.delete("event", "event=?", new String[] { "node" });
   }
 
   public static void clearHistoryEvents(DatabaseManager dbManager, int date)
   {
     SQLiteDatabase database = dbManager.getWritableDatabase();
     database.delete("event", "send=? and date<?", new String[] { "1", Integer.toString(date) });
   }
 
   public static void addEvent(DatabaseManager dbManager, String event, int date, int hour, String value, int count, long duration, boolean first, long time)
   {
     addEvent(dbManager, event, date, hour, value, count, 0, duration, first, time);
   }
 
   public static void addEvent(DatabaseManager dbManager, String event, int date, int hour, String value)
   {
     addEvent(dbManager, event, date, hour, value, 1, 0L, false, 0L);
   }
 
   public static void signSentEvent(DatabaseManager dbManager, List<Integer> idList)
   {
     SQLiteDatabase db = dbManager.getWritableDatabase();
     for (Iterator i$ = idList.iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();
       ContentValues cv = new ContentValues();
       cv.put("send", "1");
       db.update("event", cv, "id=?", new String[] { Integer.toString(id) });
     }
   }
 
   public String toString()
   {
     return "{id=" + this.id + ", event=" + this.event + ", date=" + this.date + ", hour=" + this.hour + ", value=" + this.value + ", count=" + this.count + ", exit=" + this.exit + ", duration=" + this.duration + ", first=" + this.first + ", time=" + this.time + ", send=" + this.send + "}";
   }
 
   public int getId()
   {
     return this.id;
   }
 
   public void setId(int id) {
     this.id = id;
   }
 
   public int getDate() {
     return this.date;
   }
 
   public void setDate(int date) {
     this.date = date;
   }
 
   public int getHour() {
     return this.hour;
   }
 
   public void setHour(int hour) {
     this.hour = hour;
   }
 
   public String getEvent() {
     return this.event;
   }
 
   public void setEvent(String event) {
     this.event = event;
   }
 
   public String getValue() {
     return this.value;
   }
 
   public void setValue(String value) {
     this.value = value;
   }
 
   public int getCount() {
     return this.count;
   }
 
   public void setCount(int count) {
     if (count > 0)
       this.count = count;
   }
 
   public int getExit()
   {
     return this.exit;
   }
 
   public void setExit(int exit) {
     if (exit > 0)
       this.exit = exit;
   }
 
   public long getDuration()
   {
     return this.duration;
   }
 
   public void setDuration(long duration) {
     if (duration > 0L)
       this.duration = duration;
   }
 
   public boolean isFirst()
   {
     return this.first;
   }
 
   public void setFirst(boolean first) {
     this.first = first;
   }
 
   public long getTime() {
     return this.time;
   }
 
   public void setTime(long time) {
     this.time = time;
   }
 
   public boolean isSend() {
     return this.send;
   }
 
   public void setSend(boolean send) {
     this.send = send;
   }
 }
