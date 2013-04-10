 package com.coder.android.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseManager extends SQLiteOpenHelper
 {
   private static final String LOG_TAG = "Mofang_MFDatabaseHelper";
   private static final String DATABASE = "mofang_data_analysis.db";
   private static final int VERSION = 1;
   private static final String INIT_SQL = "CREATE TABLE IF NOT EXISTS event (id INTEGER PRIMARY KEY, date INTEGER NOT NULL, hour INTEGER NOT NULL, event TEXT NOT NULL, value TEXT, count INTEGER DEFAULT 1, exit INTEGER DEFAULT 0, duration INTEGER DEFAULT 0, first INTEGER DEFAULT 0, time NUMERIC NOT NULL, send INTEGER DEFAULT 0);";
   private static final String UPGRADE_SQL = null;
 
   private static DatabaseManager self = null;
 
   private DatabaseManager(Context context)
   {
     super(context, "mofang_data_analysis.db", null, 1);
   }
 
   public static synchronized DatabaseManager getInstance(Context context)
   {
     if ((self == null) && (context != null)) {
       self = new DatabaseManager(context);
     }
     return self;
   }
 
   public void onCreate(SQLiteDatabase database)
   {
     String[] initSqls = "CREATE TABLE IF NOT EXISTS event (id INTEGER PRIMARY KEY, date INTEGER NOT NULL, hour INTEGER NOT NULL, event TEXT NOT NULL, value TEXT, count INTEGER DEFAULT 1, exit INTEGER DEFAULT 0, duration INTEGER DEFAULT 0, first INTEGER DEFAULT 0, time NUMERIC NOT NULL, send INTEGER DEFAULT 0);".split(";");
     for (String initSql : initSqls)
     {
       database.execSQL(initSql + ";");
     }
   }
 
   public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
   {
     String[] upgradeSqls = UPGRADE_SQL.split(";");
     for (String upgradeSql : upgradeSqls)
     {
       database.execSQL(upgradeSql + ";");
     }
     onCreate(database);
   }
 }

/* Location:           C:\Users\dream\Desktop\imofan.jar
 * Qualified Name:     com.imofan.android.basic.MFDatabaseManager
 * JD-Core Version:    0.6.0
 */
