package hyn.com.datastorage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hyn.com.lib.TwoTuple;

/**
 * Created by hanyanan on 2015/2/28.
 * The list structure as follow:
 * |------------------------------------------------------------------------  list  ------------------------------------------------------------------------------------|
 * |- primaryKey -|---- key ----|---- priority ----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- 0001 ----|---- list1 --|----1232131321----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- 0002 ----|---- list1 --|----1232122222----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * Column key1 and key2 as te primary key.
 */
public class BasicDataBaseHelper extends BaseSQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String QUEUE_TABLE_NAME = "queue_table";
    public static final String MAP_TABLE_NAME = "map_table";
    public static final String ORDER_TABLE_NAME = "order_table";


    public static final String CREATE_MAP_TABLE_SQL = String.format("CREATE TABLE %s\n" +
            "(\n" +
            "%s CHAR(16) PRIMARY KEY,\n" + //key
            "%s TEXT,\n" + //raw key
            "%s LONG,\n" + //expired time
            "%s BLOB\n" + //content
            ")\n",MAP_TABLE_NAME,Column.KEY, Column.RAW_KEY, Column.EXPIRE_TIME, Column.CONTENT);

    public static final String CREATE_QUEUE_TABLE_SQL = String.format("CREATE TABLE %s\n" +
                    "(\n" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT,\n" +//primary id
                    "%s CHAR(16),\n" + //key
                    "%s TEXT,\n" + //raw key
                    "%s LONG,\n" + //expired time
                    "%s LONG,\n" + //priority
                    "%s INTEGER,\n" + //size
                    "%s BLOB\n" + //content
                    ")\n",QUEUE_TABLE_NAME, Column.ID, Column.KEY, Column.RAW_KEY, Column.EXPIRE_TIME,
            Column.PRIORITY, Column.SIZE, Column.CONTENT);

    public static final String CREATE_ORDER_TABLE_SQL = String.format("CREATE TABLE %s\n" +
                    "(\n" +
                    "%s CHAR(16),\n" + //tag
                    "%s CHAR(16),\n" + //key
                    "%s TEXT,\n"+ //raw tag
                    "%s TEXT,\n"+ //raw key
                    "%s INTEGER,\n" + //size
                    "%s LONG,\n" + //access time
                    "%s LONG,\n" + //modify time
                    "%s LONG,\n" + //create time
                    "%s LONG,\n" + //expire time
                    "%s BLOB,\n" + //content
                    "PRIMARY KEY (%s,%s)"+
                    ")\n",ORDER_TABLE_NAME, Column.TAG, Column.KEY,Column.RAW_TAG, Column.RAW_KEY, Column.SIZE,
            Column.LAST_ACCESS_TIME,  Column.MODIFY_TIME, Column.CREATE_TIME,
            Column.EXPIRE_TIME, Column.CONTENT,  Column.TAG, Column.KEY);

    public BasicDataBaseHelper(Context context, String path) {
        super(context, path, null, VERSION);
    }
    public BasicDataBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MAP_TABLE_SQL);
        db.execSQL(CREATE_QUEUE_TABLE_SQL);
        db.execSQL(CREATE_ORDER_TABLE_SQL);
    }
//
//    public void put(final String key, final byte[] content, long priority, long expireTime, long systemTime){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(Column.KEY, key);
//        contentValues.put(Column.content, content);
//        contentValues.put(Column.PRIORITY, priority);
//        contentValues.put(Column.expireTime, expireTime);
//        contentValues.put(Column.SIZE, content.length);
//        contentValues.put(Column.accessTime, systemTime);
//        contentValues.put(Column.modifyTime, systemTime);
//        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
//    }
//
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
//
//    public void deleteAll(final String key){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(Column.KEY, key);
//        String sql = "DELETE FROM " + TABLE_NAME +" where "+Column.KEY+"='"+key+"'";
//        Log.d("QueueHedisDataBaseHelper deleteAll run sql " + sql);
//        getWritableDatabase().execSQL(sql);
//    }
//
//    /**
//     * Get the first element of current queue
//     * @param key
//     * @param accessTime
//     *              if it's more big than 0, then update last access time to data base, other wise do nothing.
//     * @return
//     */
//    public TwoTuple<String,byte[]> getHead(final String key, final long accessTime){
//        String sql = "select "+Column.PRIMARY_KEY+" , "+Column.content+
//                " from " + TABLE_NAME + " where " + Column.KEY +"='"+key+
//                "' order by "+Column.PRIORITY+" ASC limit 1 ";
//        Log.d("QueueHedisDataBaseHelper getHead run sql " + sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()) return null;
//        Log.d("QueueHedisDataBaseHelper getHead  cursor count " +cursor.getCount());
//        long id = cursor.getLong(cursor.getColumnIndex(Column.PRIMARY_KEY));
//        byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
//        if(accessTime > 0){
////            String selector = " "+Column.PRIMARY_KEY+"=? ";
////            ContentValues contentValues = new ContentValues();
////            contentValues.put(Column.accessTime, accessTime);
////            getWritableDatabase().updateWithOnConflict(TABLE_NAME,contentValues,selector,
////                    new String[]{""+id}, SQLiteDatabase.CONFLICT_REPLACE);
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(Column.accessTime, accessTime);
//            contentValues.put(Column.PRIMARY_KEY, id);
//            getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
//        }
//        cursor.close();
//        return new TwoTuple<String,byte[]>(key,body);
//    }
//
//    /**
//     * Get the tail element of current queue
//     * @param key
//     * @param accessTime
//     *              if it's more big than 0, then update last access time to data base, other wise do nothing.
//     * @return
//     */
//    public TwoTuple<String,byte[]> getTail(final String key, final long accessTime){
//        String sql = "select "+Column.PRIMARY_KEY+" , "+Column.content+" from " +
//                TABLE_NAME + " where " + Column.KEY +"='"+key+"' order by "+
//                Column.PRIORITY+" desc limit 1 ";
//        Log.d("QueueHedisDataBaseHelper getTail  run sql " + sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()) return null;
//        Log.d("QueueHedisDataBaseHelper getTail cursor count "+cursor.getCount());
//        long id = cursor.getLong(cursor.getColumnIndex(Column.PRIMARY_KEY));
//        byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
//        if(accessTime > 0){
//            String selector = " "+Column.PRIMARY_KEY+"=? ";
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(Column.accessTime, accessTime);
////            getWritableDatabase().updateWithOnConflict(TABLE_NAME,contentValues,selector,
////                    new String[]{""+id}, SQLiteDatabase.CONFLICT_REPLACE);
//            contentValues.put(Column.PRIMARY_KEY, id);
//            getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
//        }
//        cursor.close();
//        return new TwoTuple<String,byte[]>(key,body);
//    }
//
//    /**
//     * Query all content.
//     * @param key
//     * @return
//     */
//    public Collection<TwoTuple<String,byte[]>> queryAll(final String key, final long accessTime){
//        String sql = "select "+Column.content+" from " + TABLE_NAME + " where "
//                + Column.KEY +"='"+key+"' order by "+Column.PRIORITY+" ASC";
//        Log.d("QueueHedisDataBaseHelper queryAll run sql " + sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()) return null;
//        Log.d("QueueHedisDataBaseHelper queryAll  cursor count "+cursor.getCount());
//        final Collection<TwoTuple<String,byte[]>> res = new ArrayList<>();
//        do{
//            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
//            TwoTuple<String,byte[]> element = new TwoTuple<String,byte[]>(key,body);
//            res.add(element);
//        }while (cursor.moveToNext());
//        cursor.close();
//
//        if(accessTime > 0){//update access time
//            String updateSql = "UPDATE "+TABLE_NAME+" SET "+Column.accessTime+"="+accessTime
//                    +" WHERE "+Column.KEY+"="+key;
//            getWritableDatabase().execSQL(updateSql);
//        }
//
//        return res;
//    }
//
//    public int getGroupCount(final String key){
//        final String sql = String.format("SELECT COUNT(%s) as count from %s where %s='%s'",
//                Column.PRIMARY_KEY,TABLE_NAME,Column.KEY, key);
//        Log.d("QueueHedisDataBaseHelper  queryAll run sql " + sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
//        if(null == cursor || !cursor.moveToFirst()) return 0;
//        Log.d("QueueHedisDataBaseHelper getGroupCount cursor count " +cursor.getCount());
//        int count = cursor.getInt(cursor.getColumnIndex("count"));
//        cursor.close();
//        return count;
//    }
//
//    public int getGroupSize(final String key){
//        final String sql = String.format("SELECT SUM(%s) as size from %s where %s='%s'",
//                Column.SIZE,TABLE_NAME,Column.KEY, key);
//        Log.d("QueueHedisDataBaseHelper getGroupSize run sql "+ sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
//        if(null == cursor || !cursor.moveToFirst()) return 0;
//        Log.d("QueueHedisDataBaseHelper getGroupSize cursor count " +cursor.getCount());
//        int count = cursor.getInt(cursor.getColumnIndex("size"));
//        cursor.close();
//        return count;
//    }
//
//    /**
//     * Return the list of element which include id and size.
//     * @param key
//     * @return
//     */
//    public Collection<TwoTuple<Integer,Integer>> querySimple(final String key){
//        String sql = "select "+Column.PRIMARY_KEY+","+Column.SIZE+" from "
//                + TABLE_NAME + " where " + Column.KEY +"='"+key+"' order by "+Column.PRIORITY+" ASC";
//        Log.d("QueueHedisDataBaseHelper.querySimple run sql "+sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()) return null;
//        Log.d("QueueHedisDataBaseHelper.querySimple cursor count "+cursor.getCount());
//        final Collection<TwoTuple<Integer,Integer>> res = new ArrayList<>();
//        while (cursor.moveToNext()){
//            int id = cursor.getInt(cursor.getColumnIndex(Column.PRIMARY_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            TwoTuple<Integer,Integer> element = new TwoTuple<Integer,Integer>(id, size);
//            res.add(element);
//        }
//        cursor.close();
//        return res;
//    }
//
//    /**
//     * It's may be cause memory leak.
//     * @param key
//     * @param maxCount
//     * @return
//     */
//    public Collection<TwoTuple<String,byte[]>> trimCountWithResult(final String key, int maxCount){
//        //get all elements that need delete from data base.
//        String query = String.format("select %s, %s from %s where %s='%s' and %s not in (" +
//                "select %s from %s where %s='%s' order by %s ASC limit %s)",
//                Column.PRIMARY_KEY, Column.content,TABLE_NAME, Column.KEY,key,Column.PRIMARY_KEY,
//                Column.PRIMARY_KEY, TABLE_NAME,Column.KEY,key,Column.PRIORITY, maxCount);
//        Log.d("QueueHedisDataBaseHelper trimCountWithResult query sql " + query);
//        Cursor cursor = getReadableDatabase().rawQuery(query, null);
//        if(null == cursor || !cursor.moveToFirst()) return null;
//        Log.d("QueueHedisDataBaseHelper trimCountWithResult  query result count " +cursor.getCount());
//        final Collection<TwoTuple<String,byte[]>> result = new ArrayList<>();
//        final List<Integer> ids = new ArrayList<>();
//        do{
//            int id = cursor.getInt(cursor.getColumnIndex(Column.PRIMARY_KEY));
//            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
//            result.add(new TwoTuple<String, byte[]>(key,body));
//            ids.add(id);
//        }while(cursor.moveToNext());
//        cursor.close();
//        if(ids.size() <= 0) return result;
//
//        String deleteSql = "delete from "+TABLE_NAME+" where "+Column.PRIMARY_KEY+" in ( %s )";
//        String selector = "";
//        for(int i = 0;i<ids.size();++i){
//            if(i>0){
//                selector = selector+","+ids.get(i);
//            }else{
//                selector = ""+ids.get(i);
//            }
//        }
//        deleteSql = String.format(deleteSql,selector);
//        Log.d("QueueHedisDataBaseHelper trimCountWithResult deleteSql sql " + deleteSql);
//
//        getWritableDatabase().execSQL(deleteSql);
//        return result;
//    }
//
//    public void trimCountSilence(final String key, int maxCount){
//        //get all elements that need delete from data base.
//        String deleteSql = String.format("delete from %s where %s='%s' and %s not in (" +
//                        "select %s from %s where %s='%s' order by %s limit %s)",TABLE_NAME,
//                Column.KEY,key,Column.PRIMARY_KEY,
//                Column.PRIMARY_KEY, TABLE_NAME,Column.KEY,key,Column.PRIORITY, maxCount);
//        Log.d("QueueHedisDataBaseHelper trimCountSilence  delete sql " + deleteSql);
//        getWritableDatabase().execSQL(deleteSql);
//    }
//
//    public void deleteByIdsSilence(final List<Integer> ids){
//        if(null == ids || ids.size() <= 0) return ;
//        String deleteSql = "delete from "+TABLE_NAME+" where "+Column.PRIMARY_KEY+" in ( %s )";
//        String selector = "";
//        for(int i = 0;i<ids.size();++i){
//            if(i>0){
//                selector = selector+","+ids.get(i);
//            }else{
//                selector = ""+ids.get(i);
//            }
//        }
//        deleteSql = String.format(deleteSql,selector);
//        Log.d("QueueHedisDataBaseHelper deleteByIdsSilence deleteSql sql " + deleteSql);
//
//        getWritableDatabase().execSQL(deleteSql);
//    }
//
//    public Collection<TwoTuple<String,byte[]>> deleteByIdsWithResult(final String key, final List<Integer> ids){
//        if(null == ids || ids.size() <= 0) return null;
//        String querySql = "select "+Column.content+" from "+TABLE_NAME+" where "+Column.PRIMARY_KEY+" in ( %s )";
//        String selector = "";
//        for(int i = 0;i<ids.size();++i){
//            if(i>0){
//                selector = selector+","+ids.get(i);
//            }else{
//                selector = ""+ids.get(i);
//            }
//        }
//        querySql = String.format(querySql,selector);
//        querySql = querySql + " order by "+Column.PRIORITY+" ASC";
//        Log.d("QueueHedisDataBaseHelper  deleteByIdsSilence deleteSql sql " + querySql);
//
//        Cursor cursor = getReadableDatabase().rawQuery(querySql,null);
//        final Collection<TwoTuple<String,byte[]>> result = new ArrayList<>();
//        do{
//            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
//            result.add(new TwoTuple<String, byte[]>(key,body));
//        }while(cursor.moveToNext());
//        cursor.close();
//        deleteByIdsSilence(ids);
//        return result;
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
