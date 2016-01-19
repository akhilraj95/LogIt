package com.achilles.akhilraj.logit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by akhil on 12/24/2015.
 */

public class DBHelper extends SQLiteOpenHelper {

    //table videolog
    public static final String KEY_ID ="_id";
    public static final String KEY_NAME ="name";
    public static final String KEY_TIMESTAMP ="timestamp";
    public static final String KEY_LOGTYPE ="logtype";

    //table video
    public  static final String VID_ID = "_id";
    public static final String VID_KEY_ID ="logId";
    public static final String VID_DIR = "vidDir";
    public static final String VID_THUMBNAIL_DIR = "thumbnaildir";
    public static final String VID_TIMESTAMP = "timestamp";
    public static final String VID_LENGTH = "length";

    //table completedvideo
    public static final String COMPLETEDVID_ID = "_id";
    public static final String COMPLETEDVID_NAME = "name";
    public static final String COMPLETEDVID_DIR = "dir";
    public static final String COMPLETEDVID_TIMESTAMP = "timestamp";
    public static final String COMPLETEDVID_THUMBNAIL_DIR = "thumbnaildir";

    public static final String DATABASE_NAME = "LogItDb.db";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table videolog (" + KEY_ID + " integer primary key autoincrement , " + KEY_NAME + " text ,"+KEY_LOGTYPE+" INTEGER ," + KEY_TIMESTAMP + " DATETIME)");
        db.execSQL("create table video(" + VID_ID + " integer primary key autoincrement , " + VID_KEY_ID + " integer ," + VID_DIR + " varchar(50)," + VID_TIMESTAMP + " DATETIME ," + VID_LENGTH + " integer," + VID_THUMBNAIL_DIR + " varchar(50))");
        db.execSQL("create table completedvideo(" + COMPLETEDVID_ID + " integer primary key autoincrement, " + COMPLETEDVID_NAME + " text, " + COMPLETEDVID_DIR + " varchar(50), " + COMPLETEDVID_TIMESTAMP + " DATETIME," + VID_THUMBNAIL_DIR + " varchar(50))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS videolog");
        onCreate(db);
    }

    public boolean insert(String name,int logtype)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO videolog("+KEY_NAME+","+KEY_LOGTYPE+","+KEY_TIMESTAMP+") VALUES(?,"+String.valueOf(logtype)+",datetime('NOW'))";
        db.execSQL(sql, new String[]{name});
        return true;
    }

    public boolean newvideo(String id,String dir,int length)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO video("+VID_DIR+","+VID_TIMESTAMP+","+VID_LENGTH+","+VID_KEY_ID+","+VID_THUMBNAIL_DIR+") VALUES('"+String.valueOf(dir)+"',datetime('NOW'),'"+String.valueOf(length)+"',"+String.valueOf(id)+",'00')";
        db.execSQL(sql);
        return true;
    }

    public boolean newcompletedvideo(String name, String dir)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO completedvideo("+COMPLETEDVID_NAME+","+COMPLETEDVID_DIR+","+COMPLETEDVID_TIMESTAMP+","+COMPLETEDVID_THUMBNAIL_DIR+") VALUES('"+name+"','"+dir+"',datetime('NOW'),'00')";
        db.execSQL(sql);
        return true;
    }

    public boolean delete(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("videolog", KEY_ID + "=" + id, null) > 0;
    }

    public boolean deletevideo(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("video", VID_KEY_ID + "=" + id, null) > 0;
    }
    public boolean deletenewcompletedvideo(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("completedvideo", COMPLETEDVID_ID + "=" + id, null) > 0;
    }
    public  boolean updatethumb(String dir,String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE video set "+VID_THUMBNAIL_DIR+" = '"+dir+"' where "+VID_ID+" = '"+id+"'";
        db.execSQL(sql);
        return true;
    }

    public  boolean updatethumbcompletedvid(String dir,String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE completedvideo set "+COMPLETEDVID_THUMBNAIL_DIR+" = '"+dir+"' where "+COMPLETEDVID_ID+" = '"+id+"'";
        db.execSQL(sql);
        return true;
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select "+KEY_ID+","+KEY_NAME+","+KEY_TIMESTAMP+","+KEY_LOGTYPE+" from videolog order by "+KEY_ID+" DESC ", null );
        return res;
    }

    public Cursor getVideo(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select "+VID_ID+","+VID_DIR+","+VID_TIMESTAMP+","+VID_LENGTH+","+VID_THUMBNAIL_DIR+" from video where "+VID_KEY_ID+"='"+id+"' order by "+VID_ID+" ASC", null );
        return res;
    }

    public Cursor getCompletedVideo() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select "+COMPLETEDVID_ID+", "+COMPLETEDVID_NAME+","+COMPLETEDVID_TIMESTAMP+","+COMPLETEDVID_DIR+","+COMPLETEDVID_THUMBNAIL_DIR+" from completedvideo order by "+COMPLETEDVID_ID+" DESC",null);
        return res;

    }

    public Cursor getDataAtIndex(int i){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select "+KEY_NAME+","+KEY_TIMESTAMP+" from videolog where KEY_ID=",null);
        return res;
    }

}
