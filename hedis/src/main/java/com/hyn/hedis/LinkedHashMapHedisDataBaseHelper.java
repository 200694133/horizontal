package com.hyn.hedis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hyn.com.lib.ThreeTuple;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.Log;

/**
 * Created by hanyanan on 2015/2/28.
 *  the list structure as follow:
 * |------------------------------------------------------------------------  linkedHashMap  ------------------------------------------------------------------------------------|
 * |---- tag -----|---- key ----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- map1 ----|---- 111 ----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- map1 ----|---- 222 ----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * Column key1 and key2 as te primary key.
 * When user add/update/read will be influence accessTime element.
 * @see QueueHedis
 */
public class LinkedHashMapHedisDataBaseHelper extends BaseSQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String TABLE_NAME = "LINKED_HASHMAP_TABLE";

    public static final String CREATE_TABLE_SQL = String.format("CREATE TABLE if not exists %s\n" +
            "(\n" +
            "%s CHAR(16),\n" + //tag
            "%s CHAR(16),\n" + //key
            "%s int,\n" + //size
            "%s long,\n" + //access time
            "%s long,\n" + //modify time
            "%s TimeStamp NOT NULL DEFAULT (datetime('now','localtime')),\n" + //create time
            "%s long,\n" + //expire time
            "%s blob\n" + //content
            "primary key (%s,%s)"+
            ")\n",TABLE_NAME, Column.TAG, Column.KEY,Column.SIZE, Column.accessTime,
                    Column.modifyTime, Column.createTime, Column.expireTime, Column.content,
                    Column.TAG, Column.KEY);


    public LinkedHashMapHedisDataBaseHelper(Context context, String dbName) {
        super(context, dbName, null, VERSION);
    }

    public LinkedHashMapHedisDataBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    public void put(final String tag, final String key, final byte[] content, long expireTime, long systemTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.TAG, ValueUtil.md5_16(tag));
        contentValues.put(Column.KEY, ValueUtil.md5_16(key));
        contentValues.put(Column.content, content);
        contentValues.put(Column.expireTime, expireTime);
        contentValues.put(Column.SIZE, content.length);
        contentValues.put(Column.accessTime, systemTime);
        contentValues.put(Column.modifyTime, systemTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void put(final String tag, final String key, final byte[] content,long systemTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.TAG, ValueUtil.md5_16(tag));
        contentValues.put(Column.KEY, ValueUtil.md5_16(key));
        contentValues.put(Column.content, content);
        contentValues.put(Column.expireTime, Long.MAX_VALUE);
        contentValues.put(Column.SIZE, content.length);
        contentValues.put(Column.accessTime, systemTime);
        contentValues.put(Column.modifyTime, systemTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }


//    public void deleteTailByPriority(final String key){
//        String sql = "delete from " + TABLE_NAME + " where " + Column.PRIMARY_KEY+" in (" +
//                "select "+Column.PRIMARY_KEY+" from " + TABLE_NAME + " where "
//                + Column.KEY +"='"+key+"' order by "+Column.PRIORITY+" desc limit 1 "+
//                ")";
//        Log.d("QueueHedisDataBaseHelper  deleteTailByPriority run sql "+sql);
//        getWritableDatabase().execSQL(sql);
//    }
//
//    public void deleteHeadByPriority(final String key){
//        String sql = "delete from " + TABLE_NAME + " where " + Column.PRIMARY_KEY+" in (" +
//                "select "+Column.PRIMARY_KEY+" from " + TABLE_NAME + " where "
//                + Column.KEY +"='"+key+"' order by "+Column.PRIORITY+" ASC limit 1 "+
//                ")";
//        Log.d("QueueHedisDataBaseHelper deleteHeadByPriority run sql "+sql);
//        getWritableDatabase().execSQL(sql);
//    }

    public void deleteTag(final String tag){
        String sql = "DELETE FROM " + TABLE_NAME +" where "+Column.TAG+"='"+tag+"'";
        Log.d("QueueHedisDataBaseHelper deleteAll run sql " + sql);
        getWritableDatabase().execSQL(sql);
    }

    public void deleteEntry(final String tag, final String key){
        String sql = "DELETE FROM " + TABLE_NAME +" where "+Column.TAG+"='"+tag+"' AND "
                +Column.KEY+"='"+key+"'";
        Log.d("QueueHedisDataBaseHelper deleteAll run sql " + sql);
        getWritableDatabase().execSQL(sql);
    }

    public ThreeTuple<String,String,byte[]> get(final String tag, final String key){
        String sql = "SELECT "+Column.content+" FROM " + TABLE_NAME +" where "+Column.TAG+"='"+tag+"' AND "
                +Column.KEY+"='"+key+"'";
        Log.d("QueueHedisDataBaseHelper getEntry run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) return null;
        byte[] content = parseEntry(cursor);
        cursor.close();
        if(null == content) return null;
        return new ThreeTuple<>(tag, key, content);
    }

    public ThreeTuple<String,String,byte[]> getEldest(final String tag, OrderPolicy orderPolicy){
        String sql = "SELECT "+Column.content+" FROM " + TABLE_NAME +" where "+Column.TAG+"='"+tag+"'"
                + getOrderSelection(orderPolicy)
                + "  ";
        Log.d("QueueHedisDataBaseHelper getEntry run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) return null;
        byte[] content = parseEntry(cursor);
        cursor.close();
        if(null == content) return null;
        return new ThreeTuple<>(tag, key, content);
    }


    /**
     * Get the first element of current queue
     * @param key
     * @param accessTime
     *              if it's more big than 0, then update last access time to data base, other wise do nothing.
     * @return
     */
    public TwoTuple<String,byte[]> getHead(final String key, final long accessTime){
        String sql = "select "+Column.PRIMARY_KEY+" , "+Column.content+
                " from " + TABLE_NAME + " where " + Column.KEY +"='"+key+
                "' order by "+Column.PRIORITY+" ASC limit 1 ";
        Log.d("QueueHedisDataBaseHelper getHead run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) return null;
        Log.d("QueueHedisDataBaseHelper getHead  cursor count " +cursor.getCount());
        long id = cursor.getLong(cursor.getColumnIndex(Column.PRIMARY_KEY));
        byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
        if(accessTime > 0){
//            String selector = " "+Column.PRIMARY_KEY+"=? ";
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(Column.accessTime, accessTime);
//            getWritableDatabase().updateWithOnConflict(TABLE_NAME,contentValues,selector,
//                    new String[]{""+id}, SQLiteDatabase.CONFLICT_REPLACE);
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.accessTime, accessTime);
            contentValues.put(Column.PRIMARY_KEY, id);
            getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        cursor.close();
        return new TwoTuple<String,byte[]>(key,body);
    }

    /**
     * Get the tail element of current queue
     * @param key
     * @param accessTime
     *              if it's more big than 0, then update last access time to data base, other wise do nothing.
     * @return
     */
    public TwoTuple<String,byte[]> getTail(final String key, final long accessTime){
        String sql = "select "+Column.PRIMARY_KEY+" , "+Column.content+" from " +
                TABLE_NAME + " where " + Column.KEY +"='"+key+"' order by "+
                Column.PRIORITY+" desc limit 1 ";
        Log.d("QueueHedisDataBaseHelper getTail  run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) return null;
        Log.d("QueueHedisDataBaseHelper getTail cursor count "+cursor.getCount());
        long id = cursor.getLong(cursor.getColumnIndex(Column.PRIMARY_KEY));
        byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
        if(accessTime > 0){
            String selector = " "+Column.PRIMARY_KEY+"=? ";
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.accessTime, accessTime);
//            getWritableDatabase().updateWithOnConflict(TABLE_NAME,contentValues,selector,
//                    new String[]{""+id}, SQLiteDatabase.CONFLICT_REPLACE);
            contentValues.put(Column.PRIMARY_KEY, id);
            getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        cursor.close();
        return new TwoTuple<String,byte[]>(key,body);
    }

    /**
     * Query all content.
     * @param key
     * @return
     */
    public Collection<TwoTuple<String,byte[]>> queryAll(final String key, final long accessTime){
        String sql = "select "+Column.content+" from " + TABLE_NAME + " where "
                + Column.KEY +"='"+key+"' order by "+Column.PRIORITY+" ASC";
        Log.d("QueueHedisDataBaseHelper queryAll run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) return null;
        Log.d("QueueHedisDataBaseHelper queryAll  cursor count "+cursor.getCount());
        final Collection<TwoTuple<String,byte[]>> res = new ArrayList<>();
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            TwoTuple<String,byte[]> element = new TwoTuple<String,byte[]>(key,body);
            res.add(element);
        }while (cursor.moveToNext());
        cursor.close();

        if(accessTime > 0){//update access time
            String updateSql = "UPDATE "+TABLE_NAME+" SET "+Column.accessTime+"="+accessTime
                    +" WHERE "+Column.KEY+"="+key;
            getWritableDatabase().execSQL(updateSql);
        }

        return res;
    }

    public int getGroupCount(final String key){
        final String sql = String.format("SELECT COUNT(%s) as count from %s where %s='%s'",
                Column.PRIMARY_KEY,TABLE_NAME,Column.KEY, key);
        Log.d("QueueHedisDataBaseHelper  queryAll run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
        if(null == cursor || !cursor.moveToFirst()) return 0;
        Log.d("QueueHedisDataBaseHelper getGroupCount cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count;
    }

    public int getGroupSize(final String key){
        final String sql = String.format("SELECT SUM(%s) as size from %s where %s='%s'",
                Column.SIZE,TABLE_NAME,Column.KEY, key);
        Log.d("QueueHedisDataBaseHelper getGroupSize run sql "+ sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
        if(null == cursor || !cursor.moveToFirst()) return 0;
        Log.d("QueueHedisDataBaseHelper getGroupSize cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("size"));
        cursor.close();
        return count;
    }

    /**
     * Return the list of element which include id and size.
     * @param key
     * @return
     */
    public Collection<TwoTuple<Integer,Integer>> querySimple(final String key){
        String sql = "select "+Column.PRIMARY_KEY+","+Column.SIZE+" from "
                + TABLE_NAME + " where " + Column.KEY +"='"+key+"' order by "+Column.PRIORITY+" ASC";
        Log.d("QueueHedisDataBaseHelper.querySimple run sql "+sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) return null;
        Log.d("QueueHedisDataBaseHelper.querySimple cursor count "+cursor.getCount());
        final Collection<TwoTuple<Integer,Integer>> res = new ArrayList<>();
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(Column.PRIMARY_KEY));
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            TwoTuple<Integer,Integer> element = new TwoTuple<Integer,Integer>(id, size);
            res.add(element);
        }
        cursor.close();
        return res;
    }

    /**
     * It's may be cause memory leak.
     * @param key
     * @param maxCount
     * @return
     */
    public Collection<TwoTuple<String,byte[]>> trimCountWithResult(final String key, int maxCount){
        //get all elements that need delete from data base.
        String query = String.format("select %s, %s from %s where %s='%s' and %s not in (" +
                "select %s from %s where %s='%s' order by %s ASC limit %s)",
                Column.PRIMARY_KEY, Column.content,TABLE_NAME, Column.KEY,key,Column.PRIMARY_KEY,
                Column.PRIMARY_KEY, TABLE_NAME,Column.KEY,key,Column.PRIORITY, maxCount);
        Log.d("QueueHedisDataBaseHelper trimCountWithResult query sql " + query);
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if(null == cursor || !cursor.moveToFirst()) return null;
        Log.d("QueueHedisDataBaseHelper trimCountWithResult  query result count " +cursor.getCount());
        final Collection<TwoTuple<String,byte[]>> result = new ArrayList<>();
        final List<Integer> ids = new ArrayList<>();
        do{
            int id = cursor.getInt(cursor.getColumnIndex(Column.PRIMARY_KEY));
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            result.add(new TwoTuple<String, byte[]>(key,body));
            ids.add(id);
        }while(cursor.moveToNext());
        cursor.close();
        if(ids.size() <= 0) return result;

        String deleteSql = "delete from "+TABLE_NAME+" where "+Column.PRIMARY_KEY+" in ( %s )";
        String selector = "";
        for(int i = 0;i<ids.size();++i){
            if(i>0){
                selector = selector+","+ids.get(i);
            }else{
                selector = ""+ids.get(i);
            }
        }
        deleteSql = String.format(deleteSql,selector);
        Log.d("QueueHedisDataBaseHelper trimCountWithResult deleteSql sql " + deleteSql);

        getWritableDatabase().execSQL(deleteSql);
        return result;
    }

    public void trimCountSilence(final String key, int maxCount){
        //get all elements that need delete from data base.
        String deleteSql = String.format("delete from %s where %s='%s' and %s not in (" +
                        "select %s from %s where %s='%s' order by %s limit %s)",TABLE_NAME,
                Column.KEY,key,Column.PRIMARY_KEY,
                Column.PRIMARY_KEY, TABLE_NAME,Column.KEY,key,Column.PRIORITY, maxCount);
        Log.d("QueueHedisDataBaseHelper trimCountSilence  delete sql " + deleteSql);
        getWritableDatabase().execSQL(deleteSql);
    }

    public void deleteByIdsSilence(final List<Integer> ids){
        if(null == ids || ids.size() <= 0) return ;
        String deleteSql = "delete from "+TABLE_NAME+" where "+Column.PRIMARY_KEY+" in ( %s )";
        String selector = "";
        for(int i = 0;i<ids.size();++i){
            if(i>0){
                selector = selector+","+ids.get(i);
            }else{
                selector = ""+ids.get(i);
            }
        }
        deleteSql = String.format(deleteSql,selector);
        Log.d("QueueHedisDataBaseHelper deleteByIdsSilence deleteSql sql " + deleteSql);

        getWritableDatabase().execSQL(deleteSql);
    }

    public Collection<TwoTuple<String,byte[]>> deleteByIdsWithResult(final String key, final List<Integer> ids){
        if(null == ids || ids.size() <= 0) return null;
        String querySql = "select "+Column.content+" from "+TABLE_NAME+" where "+Column.PRIMARY_KEY+" in ( %s )";
        String selector = "";
        for(int i = 0;i<ids.size();++i){
            if(i>0){
                selector = selector+","+ids.get(i);
            }else{
                selector = ""+ids.get(i);
            }
        }
        querySql = String.format(querySql,selector);
        querySql = querySql + " order by "+Column.PRIORITY+" ASC";
        Log.d("QueueHedisDataBaseHelper  deleteByIdsSilence deleteSql sql " + querySql);

        Cursor cursor = getReadableDatabase().rawQuery(querySql,null);
        final Collection<TwoTuple<String,byte[]>> result = new ArrayList<>();
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            result.add(new TwoTuple<String, byte[]>(key,body));
        }while(cursor.moveToNext());
        cursor.close();
        deleteByIdsSilence(ids);
        return result;
    }

    public static String getOrderSelection(OrderPolicy orderPolicy){
        switch (orderPolicy) {
            case LRU://按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾
                return " ORDER BY " + Column.accessTime + " ASC ";
            case TTL://最先超时的放在头部，最后超时的放在末尾
                return " ORDER BY " + Column.expireTime + " ASC ";
            case REVERT_TTL://按照超时时间排序，最先超时的放在末尾，最后超时的放在头部
                return " ORDER BY " + Column.expireTime + " DESC ";
            case Size://按照从小到大，
                return " ORDER BY " + Column.SIZE + " ASC ";
            case REVERT_SIZE://最先超时的放在头部，最后超时的放在末尾
                return " ORDER BY " + Column.SIZE + " DESC ";
            case DEFAULT:
                return "";
        }
        return "";
    }

    public static byte[] parseEntry(Cursor cursor){
        if(null == cursor) return null;
        byte[] content = cursor.getBlob(cursor.getColumnIndex(Column.content));
        return content;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
