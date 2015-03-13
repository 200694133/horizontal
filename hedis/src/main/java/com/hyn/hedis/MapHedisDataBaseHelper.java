package com.hyn.hedis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hyn.com.lib.TwoTuple;
import hyn.com.lib.android.Log;

/**
 * Created by hanyanan on 2015/2/28.
 * The list structure as follow:
 * The map structure as follow:
 * |----------------------------------------------------------  map  ------------------------------------------------------------------|
 * |---- key -----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- map1 ----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- map2 ----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |-----------------------------------------------------------------------------------------------------------------------------------|
 * column key as the primary key
 *
 * @see QueueHedis
 */
public class MapHedisDataBaseHelper extends BaseSQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String TABLE_NAME = "map_data";

    public static final String CREATE_TABLE_SQL = String.format("CREATE TABLE if not exists %s\n" +
            "(\n" +
            "%s TEXT PRIMARY KEY,\n" + //key
            "%s int,\n" + //size
            "%s long,\n" + //access time
            "%s long,\n" + //modify time
            "%s TimeStamp NOT NULL DEFAULT (datetime('now','localtime')),\n" + //create time
            "%s long,\n" + //expire time
            "%s blob\n" + //content
            ")\n",TABLE_NAME, Column.PRIMARY_KEY, Column.SIZE, Column.accessTime,
            Column.modifyTime, Column.createTime, Column.expireTime, Column.content);


    public MapHedisDataBaseHelper(Context context, String dbName) {
        super(context, dbName, null, VERSION);
    }
    public MapHedisDataBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    public static TwoTuple<String,byte[]> parseContent(String key, Cursor cursor){
        byte[] data = cursor.getBlob(cursor.getColumnIndex(Column.content));

        return new TwoTuple<>(key, data);
    }

    public void put(final String key, final byte[] content, long expireTime, long systemTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.PRIMARY_KEY, key);
        contentValues.put(Column.content, content);
        contentValues.put(Column.expireTime, expireTime);
        contentValues.put(Column.SIZE, content.length);
        contentValues.put(Column.accessTime, systemTime);
        contentValues.put(Column.modifyTime, systemTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean exits(final String key){
        final String sql = "SELECT "+Column.PRIMARY_KEY+" from "+TABLE_NAME+" WHERE "+Column.PRIMARY_KEY
                +"='"+key+"'";
        Log.d("QueueHedisDataBaseHelper exits run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
        if(null == cursor || !cursor.moveToFirst()) return false;
        cursor.close();
        return true;
    }

    public void deleteSilence(final String key){
        String sql = "DELETE FROM " + TABLE_NAME +" where "+Column.KEY+"='"+key+"'";
        Log.d("QueueHedisDataBaseHelper deleteAll run sql " + sql);
        getWritableDatabase().execSQL(sql);
    }

    public TwoTuple<String,byte[]> delete(final String key){
        TwoTuple<String,byte[]> res = get(key);
        deleteSilence(key);
        return res;
    }

    public TwoTuple<String,byte[]> get(final String key){
        TwoTuple<String,byte[]> res = null;
        String querySql = "SELECT "+Column.content+" FROM " + TABLE_NAME +" where "+Column.KEY+"='"+key+"'";
        Log.d("QueueHedisDataBaseHelper delete query run sql " + querySql);
        Cursor cursor = getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            res = null;
        }else{
            res = parseContent(key, cursor);
        }
        cursor.close();
        return res;
    }

    public int getCount(){
        final String sql = "SELECT COUNT(%s) as count from "+TABLE_NAME;
        Log.d("QueueHedisDataBaseHelper  queryAll run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
        if(null == cursor || !cursor.moveToFirst()) return 0;
        Log.d("QueueHedisDataBaseHelper getGroupCount cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count;
    }

    public int getSize(){
        final String sql = "SELECT SUM("+Column.SIZE+") as size from "+TABLE_NAME;
        Log.d("QueueHedisDataBaseHelper getGroupSize run sql "+ sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
        if(null == cursor || !cursor.moveToFirst()) return 0;
        Log.d("QueueHedisDataBaseHelper getGroupSize cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("size"));
        cursor.close();
        return count;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
