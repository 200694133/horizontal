package com.hyn.hedis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import hyn.com.lib.FourTuple;
import hyn.com.lib.IOUtil;
import hyn.com.lib.ThreeTuple;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.Log;

/**
 * Created by hanyanan on 2015/2/28.
 *  the list structure as follow:
 * |------------------------------------------------------------------------  linkedHashMap  -----------------------------------------------------------------------|
 * |---- tag -----|-- md5(key)--|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|----rawKey----|---- content ----|
 * |---- map1 ----|---- 111 ----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----111111----|----"12345678" --|
 * |---- map1 ----|---- 222 ----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----222222----|----"12332543" --|
 * |----------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * Column key1 and key2 as te primary key.
 * When user add/update/read will be influence accessTime element.
 * @see QueueHedis
 */
public class LinkedHashMapHedisDataBaseHelper extends BaseSQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String TABLE_NAME = "LINKED_HASHMAP_TABLE";

    public static final String CREATE_TABLE_SQL = String.format("CREATE TABLE %s\n" +
            "(\n" +
            "%s CHAR(16),\n" + //tag
            "%s CHAR(16),\n" + //key
            "%s TEXT,\n"+
            "%s integer,\n" + //size
            "%s long,\n" + //access time
            "%s long,\n" + //modify time
            "%s long,\n" + //create time
            "%s long,\n" + //expire time
            "%s blob,\n" + //content
            "primary key (%s,%s)"+
            ")\n",TABLE_NAME, Column.TAG, Column.KEY,Column.RAW_KEY, Column.SIZE, Column.accessTime,
                    Column.modifyTime, Column.createTime, Column.expireTime, Column.content,
                    Column.TAG, Column.KEY);

    public static WeakReference<LinkedHashMapHedisDataBaseHelper> sDBRef;

    public static synchronized LinkedHashMapHedisDataBaseHelper getInstance(Context context) {
       if(null != sDBRef && null != sDBRef.get()) return sDBRef.get();

        LinkedHashMapHedisDataBaseHelper db = new LinkedHashMapHedisDataBaseHelper(context);
        sDBRef = new WeakReference<LinkedHashMapHedisDataBaseHelper>(db);
        return db;
    }

    private LinkedHashMapHedisDataBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("onCreate "+CREATE_TABLE_SQL);
        db.execSQL(CREATE_TABLE_SQL);
    }

    private String encodedString(String s){
        return ValueUtil.md5_16(s);
    }

    private void checkTime(long expireTime, long systemTime){
        if(systemTime > expireTime) throw new IllegalArgumentException("expire time cannot before than current time.");
    }
    public void put(final String rawTag, final String rawKey, final byte[] content, long expireTime, long systemTime){
        checkTime(expireTime, systemTime);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.TAG, encodedString(rawTag));
        contentValues.put(Column.KEY, encodedString(rawKey));
        contentValues.put(Column.RAW_KEY, rawKey);
        contentValues.put(Column.content, content);
        contentValues.put(Column.expireTime, expireTime);
        contentValues.put(Column.SIZE, content.length);
        contentValues.put(Column.accessTime, systemTime);
        contentValues.put(Column.modifyTime, systemTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void put(final String rawTag, final String rawKey, final byte[] content,long systemTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.TAG, encodedString(rawTag));
        contentValues.put(Column.KEY, encodedString(rawKey));
        contentValues.put(Column.RAW_KEY, rawKey);
        contentValues.put(Column.content, content);
        contentValues.put(Column.expireTime, Long.MAX_VALUE);
        contentValues.put(Column.SIZE, content.length);
        contentValues.put(Column.accessTime, systemTime);
        contentValues.put(Column.modifyTime, systemTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Delete all out-of-date entries of current tag. May be this calling will cost much times, please
     * invoke it careful. Make sure it will not block other io operations. It's recommend to invoke
     * this function at low load of CPU and I/O.
     * @param tag the tag need to remove all invalid entries.
     * @param systemTime current system.
     */
    public void clearTrash(String tag, long systemTime){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s<=%s\n", Column.TAG, encodedString(tag), Column.expireTime, systemTime));
        String sql = sb.toString();
        Log.d("clearOutDateEntry run sql "+sql);
        getWritableDatabase().execSQL(sql);
    }
    /**
     * Get all available entries' property.
     * The property include raw key, content size, access time, expire time.
     * @param rawTag
     * @param systemTime
     * @return
     */
    public List<FourTuple<String, Integer, Long, Long>> getPropertyList(String rawTag, long systemTime){
        final List<FourTuple<String, Integer, Long, Long>>  result = new ArrayList<>();
        String tag = encodedString(rawTag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s,%s,%s\n", Column.RAW_KEY, Column.SIZE, Column.accessTime, Column.expireTime));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, tag, Column.expireTime, systemTime));
        String sql = sb.toString();

        Log.d("getPropertyList run sql "+sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        do {
            String key = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            Integer size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            Long accessTime = cursor.getLong(cursor.getColumnIndex(Column.accessTime));
            Long expireTime = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
            FourTuple<String, Integer, Long, Long> tuple = new FourTuple<>(key, size, accessTime, expireTime);
            result.add(tuple);
        }while(cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return result;
    }

    /**
     * Delete all entries under the current tag.
     */
    public void delete(final String rawTag){
        String tag = encodedString(rawTag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s'", Column.TAG, tag));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper delete run sql " + sql);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * Delete one specify entry.
     * @param rawTag
     * @param rawKey
     */
    public void delete(final String rawTag, final String rawKey){
        String tag = encodedString(rawTag);
        String key = encodedString(rawKey);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s='%s'\n", Column.TAG, tag, Column.KEY, key));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper deleteAll run sql " + sql);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * Return the specify raw tag/key/content. Update last access time.
     * @param rawTag raw tag
     * @param rawKey raw key
     * @param systemTime current wall clock time.
     * @return
     */
    public ThreeTuple<String,String,byte[]> get(String rawTag, String rawKey, long systemTime){
        String md5Tag = encodedString(rawTag);
        String md5Key = encodedString(rawKey);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.content));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s='%s'\t", Column.TAG, md5Tag, Column.KEY, md5Key));
        sb.append(String.format("AND %s>%s\n", Column.expireTime, systemTime));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper get run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return null;
        }

        byte[] content = cursor.getBlob(cursor.getColumnIndex(Column.content));
        IOUtil.safeClose(cursor);
        if(null == content || content.length <= 0) return null;
        if(systemTime > 0) updateAccessTime(rawTag, rawKey, systemTime);
        return new ThreeTuple<>(rawTag, rawKey, content);
    }

    /**
     * update access time to current system time to specify entry.
     * @param tag
     * @param key
     * @param accessTime current wall clock time.
     */
    void updateAccessTime(final String tag, final String key, long accessTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.TAG, encodedString(tag));
        contentValues.put(Column.KEY, encodedString(key));
        contentValues.put(Column.accessTime, accessTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Return the eldest entry to specify tag which order by orderPolicy.
     * @param rawTag
     * @param systemTime  current wall clock time, it's an absolute value which will not change
     *                    by system time zone.
     * @param orderPolicy
     * @return the eldest entry to specify tag which order by orderPolicy.
     */
    public ThreeTuple<String,String,byte[]> eldest(String rawTag, long systemTime, OrderPolicy orderPolicy){
        String tag = encodedString(rawTag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.content));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\t", Column.TAG, tag, Column.expireTime, systemTime));
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        sb.append(String.format("LIMIT 1\n"));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper getEntry run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
        byte[] content = cursor.getBlob(cursor.getColumnIndex(Column.content));
        IOUtil.safeClose(cursor);
        if(null == content || content.length <= 0) return null;
        updateAccessTime(tag, rawKey, systemTime);
        return new ThreeTuple<>(tag, rawKey, content);
    }


    /**
     * Query all content. Do not update access time;
     * @param tag
     * @return the list fo tag/key.content
     */
    public List<ThreeTuple<String, String,byte[]>> getAll(final String tag,
                                    final long systemTime, OrderPolicy orderPolicy){
        String md5Tag = encodedString(tag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.content));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\t", Column.TAG, md5Tag, Column.expireTime, systemTime));
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper getAll run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d("QueueHedisDataBaseHelper queryAll  cursor count "+cursor.getCount());
        final List<ThreeTuple<String, String,byte[]>> res = new ArrayList<>();
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            ThreeTuple<String, String,byte[]>element = new ThreeTuple<>(tag, rawKey,body);
            res.add(element);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    /**
     * Get a page. This function will update the access time property.
     * @param tag
     * @return The list of current page contents under the specify tag,
     */
    public List<ThreeTuple<String, String,byte[]>> getPage(String tag, long systemTime,
                                          int pageOffset, int count, OrderPolicy orderPolicy){
        String md5Tag = encodedString(tag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.content));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\t", Column.TAG, md5Tag, Column.expireTime, systemTime));
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        sb.append(String.format("LIMIT %s OFFSET %s\n", count, pageOffset*count));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper getPage run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d("QueueHedisDataBaseHelper getPage cursor count "+cursor.getCount());
        final List<ThreeTuple<String, String,byte[]>> res = new ArrayList<>();
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            ThreeTuple<String, String,byte[]>element = new ThreeTuple<>(tag, rawKey,body);
            res.add(element);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);


        StringBuilder update = new StringBuilder();
        update.append(String.format("UPDATE %s\n", TABLE_NAME));
        update.append(String.format("SET %s=%s\n", Column.accessTime, systemTime));
        update.append(String.format("WHERE %s='%s' AND %s>%s\t", Column.TAG, md5Tag, Column.expireTime, systemTime));
        update.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        update.append(String.format("LIMIT %s OFFSET %s\n", count, pageOffset*count));
        String updateSql = update.toString();
        Log.d("Update access time "+updateSql);
        getWritableDatabase().execSQL(updateSql);

        return res;
    }

    /**
     * Get the count of current tag. Do not change any property.
     * @param rawTag
     * @param systemTime
     * @return
     */
    public int getCount(final String rawTag, final long systemTime){
        String md5Tag = encodedString(rawTag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT COUNT(%s) AS COUNT\n", Column.KEY));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\t", Column.TAG, md5Tag, Column.expireTime, systemTime));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper getCount run sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return 0;
        }
        Log.d("QueueHedisDataBaseHelper getGroupCount cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("COUNT"));
        IOUtil.safeClose(cursor);
        return count;
    }

    /**
     *
     * @param tag
     * @param systemTime
     * @return
     */
    public int getSize(final String tag, final long systemTime){
        String md5Tag = encodedString(tag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT SUM(%s) AS SIZE\n", Column.SIZE));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\t", Column.TAG, md5Tag, Column.expireTime, systemTime));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper getSize run sql "+ sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return -1;
        }
        Log.d("QueueHedisDataBaseHelper getSize cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("SIZE"));
        IOUtil.safeClose(cursor);
        return count;
    }

    /**
     * Return the list of element which include tag/key/size.
     * @param tag
     * @return
     */
    public List<ThreeTuple<String, String,Integer>> getBasic(String tag,
                                          long systemTime, OrderPolicy orderPolicy){
        String md5Tag = encodedString(tag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.SIZE));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\t", Column.TAG, md5Tag, Column.expireTime, systemTime));
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper.querySimple run sql "+sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d("QueueHedisDataBaseHelper.querySimple cursor count "+cursor.getCount());
        final List<ThreeTuple<String, String,Integer>> res = new ArrayList<>();
        do{
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            ThreeTuple<String, String,Integer> element = new ThreeTuple<>(tag, rawKey,size);
            res.add(element);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    /**
     *
     * @param rawTag
     * @param systemTime must be greater than 0
     * @param maxCount must be greater than 0
     * @param orderPolicy cannot as the default mode
     * @return
     */
    public List<ThreeTuple<String, String, byte[]>> trimCountWithResult(String rawTag,
                                          long systemTime, int maxCount, OrderPolicy orderPolicy){
        //check input argument,TODO
        String tag = encodedString(rawTag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s ,%, %s\n", Column.RAW_KEY, Column.expireTime, Column.SIZE));
        sb.append(String.format("FROM %s \n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, tag, Column.expireTime, systemTime));
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        sb.append(String.format("LIMIT %s OFFSET %s\n", Integer.MAX_VALUE, maxCount));

        String query = sb.toString();
        Log.d("QueueHedisDataBaseHelper trimCountWithResult query sql " + query);
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d("QueueHedisDataBaseHelper trimCountWithResult  query result count " +cursor.getCount());
        final List<ThreeTuple<String, String, byte[]>> result = new ArrayList<>();
        do{
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            result.add(new ThreeTuple<>(tag, rawKey,body));
        }while(cursor.moveToNext());

        cursor.moveToFirst();//第一个是临界值

        sb.delete(0, sb.length());
        sb.append(String.format("DELETE FROM %s \n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, tag, Column.expireTime, systemTime));
        sb.append(String.format("AND %s\n", getDeleteWhereSelection(orderPolicy, cursor)));
        String deleteSql = sb.toString();
        Log.d("QueueHedisDataBaseHelper trimCountWithResult deleteSql sql " + deleteSql);
        getWritableDatabase().execSQL(deleteSql);
        return result;
    }

    /**
     * Delete some items
     * <b>The function will delete the out of date entries.</b>
     * @param rawTag
     * @param maxCount
     * @param orderPolicy
     */
    public void trimCount(String rawTag, int maxCount, long systemTime, OrderPolicy orderPolicy){
        String tag = encodedString(rawTag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s ,%, %s\n", Column.RAW_KEY, Column.expireTime, Column.SIZE));
        sb.append(String.format("FROM %s \n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, tag, Column.expireTime, systemTime));
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        sb.append(String.format("LIMIT 1 OFFSET %s\n", maxCount));
        String query = sb.toString();
        Log.d("QueueHedisDataBaseHelper trimCountWithResult query sql " + query);
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return ;
        }
        sb.delete(0,sb.length());
        sb.append(String.format("DELETE FROM %s \n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, tag, Column.expireTime, systemTime));
        sb.append(String.format("AND %s\n", getDeleteWhereSelection(orderPolicy, cursor)));
        String deleteSql = sb.toString();
        Log.d("QueueHedisDataBaseHelper trimCountWithResult deleteSql sql " + deleteSql);
        IOUtil.safeClose(cursor);
        getWritableDatabase().execSQL(deleteSql);
    }

    public void deleteSilence(String tag, List<String> keys){
        if(null == keys || keys.size() <= 0) return ;
        String md5Tag = ValueUtil.md5_16(tag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s \n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s'\n", Column.TAG, md5Tag));
        String selector = "";
        for(int i = 0;i<keys.size();++i){
            if(i>0){
                selector = selector+",'"+ValueUtil.md5_16(keys.get(i))+"'";
            }else{
                selector = "'"+ValueUtil.md5_16(keys.get(i))+"'";
            }
        }
        sb.append(String.format("AND %s IN(%s)\n", Column.KEY, selector));
        String deleteSql = sb.toString();
        Log.d("QueueHedisDataBaseHelper deleteByIdsSilence deleteSql sql " + deleteSql);
        getWritableDatabase().execSQL(deleteSql);
    }

    public List<ThreeTuple<String,String,byte[]>> get(String tag, List<String> keys, long systemTime){
        if(null == keys || keys.size() <= 0) return null;
        String md5Tag = ValueUtil.md5_16(tag);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s\n", Column.RAW_KEY, Column.content));
        sb.append(String.format("FROM %s \n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, md5Tag, Column.expireTime, systemTime));
        String selector = "";
        for(int i = 0;i<keys.size();++i){
            if(i>0){
                selector = selector+",'"+ValueUtil.md5_16(keys.get(i))+"'";
            }else{
                selector = "'"+ValueUtil.md5_16(keys.get(i))+"'";
            }
        }
        sb.append(String.format("AND %s IN (%s)\n", Column.KEY, selector));
        String sql = sb.toString();
        Log.d("QueueHedisDataBaseHelper get run sql " + sql);

        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return null;
        }
        final HashMap<String,byte[]> result = new HashMap<>();
        do{
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            result.put(rawKey, body);
        }while(cursor.moveToNext());
        IOUtil.safeClose(cursor);
        final List<ThreeTuple<String,String,byte[]>> res = new ArrayList<>();
        for(String rawKey : keys){
            byte[] content = result.get(rawKey);
            if(null==content) continue;
            res.add(new ThreeTuple<>(tag, rawKey,content));
        }
        return res;
    }

    public List<ThreeTuple<String,String,byte[]>> deleteWithResult(String tag, List<String> keys,long systemTime){
        List<ThreeTuple<String,String,byte[]>> res = get(tag, keys, systemTime);
        deleteSilence(tag, keys);
        return res;
    }

    public static String getQueryOrderSelection(OrderPolicy orderPolicy){
        switch (orderPolicy) {
            case LRU://按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾
                return Column.accessTime + " DESC ";
            case TTL://最先超时的放在头部，最后超时的放在末尾
                return Column.expireTime + " ASC ";
            case REVERT_TTL://按照超时时间排序，最先超时的放在末尾，最后超时的放在头部
                return Column.expireTime + " DESC ";
            case Size://按照从小到大，
                return Column.SIZE + " ASC ";
            case REVERT_SIZE://最先超时的放在头部，最后超时的放在末尾
                return Column.SIZE + " DESC ";
            case DEFAULT:
                throw new IllegalArgumentException("Cannot input default order mode!");
        }
        throw new IllegalArgumentException("Cannot input default order mode!");
    }

    public static String getDeleteWhereSelection(OrderPolicy orderPolicy, Cursor cursor){
        String where = " 1=1 ";
        switch (orderPolicy){
            case LRU://按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾
                Long accessTime = cursor.getLong(cursor.getColumnIndex(Column.accessTime));
                if(accessTime == null) break;
                where = Column.accessTime+"<="+accessTime;
                break;
            case TTL://最先超时的放在头部，最后超时的放在末尾
                Long ttl = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
                if(ttl == null) break;
                where = Column.expireTime+">="+ttl;
                break;
            case REVERT_TTL://按照超时时间排序，最先超时的放在末尾，最后超时的放在头部
                Long rttl = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
                if(rttl == null) break;
                where = Column.expireTime+"<="+rttl;
                break;
            case Size://按照从小到大，
                int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
                if(size <= 0) break;
                where = Column.SIZE+">="+size;
                break;
            case REVERT_SIZE://按照从小到大，
                int rSize = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
                if(rSize <= 0) break;
                where = Column.SIZE+"<="+rSize;
                break;
        }
        return where;
    }


    /**
     * 与默认的排序相反，例如需要读取最后一个时，可以用这个
     * @param orderPolicy
     * @return
     */
    public static String getRevertQueryOrderSelection(OrderPolicy orderPolicy){
        switch (orderPolicy) {
            case LRU://按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾
                return  Column.accessTime + " DESC ";
            case TTL://最先超时的放在头部，最后超时的放在末尾
                return  Column.expireTime + " DESC ";
            case REVERT_TTL://按照超时时间排序，最先超时的放在末尾，最后超时的放在头部
                return  Column.expireTime + " ASC ";
            case Size://按照从小到大，
                return  Column.SIZE + " DESC ";
            case REVERT_SIZE://最先超时的放在头部，最后超时的放在末尾
                return  Column.SIZE + " ASC ";
            case DEFAULT:
                throw new IllegalArgumentException("Cannot input default order mode!");
        }
        throw new IllegalArgumentException("Cannot input default order mode!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
