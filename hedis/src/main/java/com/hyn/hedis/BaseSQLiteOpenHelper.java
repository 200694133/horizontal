package com.hyn.hedis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * Created by hanyanan on 2015/2/28.
 */
public class BaseSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/data.db";
    public BaseSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class Column{
        public static final String PRIMARY_KEY = "primaryKey";
        public static final String TAG = "primaryKey";
        public static final String KEY = "key";
        public static final String PRIORITY = "priority";
        public static final String SIZE = "size";
        public static final String accessTime = "accessTime";
        public static final String modifyTime = "modifyTime";
        public static final String createTime = "createTime";
        public static final String expireTime = "expireTime";
        public static final String content = "content";
    }
}
