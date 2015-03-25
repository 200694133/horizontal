package com.hyn.hedis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.util.ArrayList;
import java.util.List;

import hyn.com.lib.IOUtil;
import hyn.com.lib.ThreeTuple;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.Log;

/**
 * Created by hanyanan on 2015/3/16.
 * Use a memory dataBase as the index to increase of efficiency, it cost the external memory to store
 * the property of current tag, when user write to disk. it will select from memory dataBase then
 * execute to the disk dataBase.
 * 1. When user trim the size of current tag, first, it calculate the sum from memory dataBase, if is
 *      smaller than the largest size then do nothing, other wise it will get all items which need
 *      to delete from memory dataBase.
 * 2. When user trim the item count of current tag, it get count from memory dataBase, if need to
 *      delete some items, it will get all primary keys then delete in disk dataBase.
 */
public class FastLinkedHashMapDataBaseHelper extends BaseSQLiteOpenHelper {
    public static interface OnEntryRemovedListener {
        public void onRemoved(String tag, String key, byte[] body);
    }
    public static final int VERSION = 1;
    public static final String TABLE_NAME = "MEMORY_DATABASE_TABLE";
    public static final String DB_NAME = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/mem.db";

    public static final String CREATE_TABLE_SQL = String.format("CREATE TABLE %s\n" +
                    "(\n" +
                    "%s CHAR(16) NOT NULL PRIMARY KEY,\n" + //key
                    "%s INTEGER,\n" + //size
                    "%s LONG,\n" + //access time
                    "%s LONG,\n" + //expire time
                    "%s TEXT\n" + //raw key
                    ")\n",
                    TABLE_NAME,
            Column.KEY,Column.SIZE, Column.accessTime, Column.expireTime, Column.RAW_KEY);



    //the real disk database which store the content.
    public FastLinkedHashMapDataBaseHelper(Context context) {
        super(context, null, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }


    String encodedString(String s){
        return ValueUtil.md5_16(s);
    }

//    /**
//     * Check if contain the key, it need to check if current item out of date, if it true then need
//     * delete from both current memory dataBase and disk dataBase.
//     * @param rawKey
//     * @param systemTime
//     * @return {@code false} mean that it does not exits or it's out-of-date,
//     *              {@code true} it's exits and it's valid.
//     */
//    public boolean exits(String rawKey,long systemTime){
//        return getSize(rawKey, systemTime)>0;
//    }
//
//    public long size(){
//        clearTrash(TimeUtils.getCurrentWallClockTime());
//        return mCurrentSize;
//    }
//    public int count(){
//        clearTrash(TimeUtils.getCurrentWallClockTime());
//        return mCurrentCount;
//    }
//
//    public void sync(final long system){
//        String clear = "DELETE FROM "+TABLE_NAME;
//        getWritableDatabase().execSQL(clear);
//        mCurrentCount = 0;
//        mCurrentSize = 0;
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT %s, %s, %s, %s\n", Column.SIZE, Column.expireTime, Column.accessTime,Column.RAW_KEY));
//        sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
//        sb.append(String.format("WHERE %s='%s' AND %s>%s",Column.TAG, encodedString(getTag()), Column.expireTime, system));
//        String sql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper sync run sql " + sql);
//        Cursor cursor = mLinkedHashMapHedisDataBaseHelper.getWritableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return ;
//        }
//
//        do{
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            Long exp = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
//            Long acc = cursor.getLong(cursor.getColumnIndex(Column.accessTime));
//            String key = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(Column.KEY, encodedString(key));
//            contentValues.put(Column.RAW_KEY, key);
//            contentValues.put(Column.expireTime, exp);
//            contentValues.put(Column.SIZE, size);
//            contentValues.put(Column.accessTime, acc);
//            getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
//            onEntryFound(key,size);
//        }while(cursor.moveToNext());
//        IOUtil.safeClose(cursor);
//
//        Log.d("FastLinkedHashMapDataBaseHelper sync size "+mCurrentSize);
//        Log.d("FastLinkedHashMapDataBaseHelper sync count "+mCurrentCount);
//    }
//
//
//    //refresh data
//    private void invalidate(){
//
//    }
//
//
//    /**
//     * Check if contain the key, it need to check if current item out of date, if it true then need
//     * delete from both current memory dataBase and disk dataBase.
//     * @param rawKey
//     * @param systemTime
//     * @return
//     */
//    private int getSize(String rawKey, long systemTime){
//        String key = encodedString(rawKey);
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT %s, %s, %s\n", Column.SIZE, Column.expireTime));
//        sb.append(String.format("FROM %s\n", TABLE_NAME));
//        sb.append(String.format("WHERE %s='%s'\t",Column.KEY, key));
//
//        String sql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper getSize run sql "+sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()) {
//            IOUtil.safeClose(cursor);
//            return -1;
//        }
//        Long expire = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
//        int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//        if(null == expire || expire <= systemTime || size <= 0){
//            IOUtil.safeClose(cursor);
//            delete(rawKey);
//            onEntryDeleted(rawKey,size);
//            return -1;
//        }
//        IOUtil.safeClose(cursor);
//        return size;
//    }
//
//    /**
//     * Delete entry from both memory dataBase and disk dataBase. Do not update any property. Do not update the {#mCurrentSize}
//     * and {#mCurrentCount}.
//     * @param rawKey
//     */
//    private void delete(String rawKey){
//        String sql = "DELETE FROM "+TABLE_NAME+" WHERE "+Column.KEY+"='"+encodedString(rawKey)+"'";
//        Log.d("FastLinkedHashMapDataBaseHelper delete sql " + sql);
//        getWritableDatabase().execSQL(sql);
//        mLinkedHashMapHedisDataBaseHelper.delete(getTag(), rawKey);
//    }
//
//    /**
//     * Called when
//     * @param rawKey
//     * @param size
//     */
//    protected synchronized void onEntryDeleted(String rawKey, int size){
//        Log.d("FastLinkedHashMapDataBaseHelper onEntryDeleted "+rawKey+" Size "+size);
//        mCurrentSize -= size;
//        mCurrentCount --;
//    }
//
//    protected synchronized void onEntryFound(String rawKey, int size){
//        Log.d("FastLinkedHashMapDataBaseHelper onEntryFound "+rawKey+" Size "+size);
//        mCurrentSize += size;
//        mCurrentCount ++;
//    }
//
//    protected synchronized void onEntryChanged(String rawKey, int oldSize, int newSize){
//        Log.d("FastLinkedHashMapDataBaseHelper onEntryChanged "+rawKey+" oldSize "+oldSize+" , newSize "+newSize);
//        mCurrentSize = mCurrentSize - oldSize + newSize;
//    }
//
//    private void checkTime(long expireTime, long systemTime){
//        if(systemTime > expireTime) throw new IllegalArgumentException("expire time cannot before than current time.");
//    }
//
//    /**
//     * Get the raw key from memory database.
//     * @param key encoded key
//     * @return real key.
//     */
//    private String getRawKey(String key){
//        String sql = "SELECT "+Column.RAW_KEY+" FROM "+TABLE_NAME+
//                " WHERE "+Column.KEY+"='"+key+"'";
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return null;
//        }
//        String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//        IOUtil.safeClose(cursor);
//        return rawKey;
//    }
//
//    private void putEntry(String rawKey, int newLength, long expireTime, long systemTime){
//        int prevSize = getSize(rawKey,systemTime);
//        if(prevSize > 0){
//            onEntryChanged(rawKey, prevSize, newLength);
//        }else{
//            onEntryFound(rawKey, newLength);
//        }
//    }
//
//    public void put(final String rawKey, final byte[] content, long expireTime, long systemTime){
//        checkTime(expireTime, systemTime);
//        putEntry(rawKey, content.length, expireTime, systemTime);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(Column.KEY, encodedString(rawKey));
//        contentValues.put(Column.RAW_KEY, rawKey);
//        contentValues.put(Column.expireTime, expireTime);
//        contentValues.put(Column.SIZE, content.length);
//        contentValues.put(Column.accessTime, systemTime);
//        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
//        mLinkedHashMapHedisDataBaseHelper.put(getTag(),rawKey,content,expireTime,systemTime);
//        Log.d("FastLinkedHashMapDataBaseHelper Put "+rawKey);
//        Log.d("FastLinkedHashMapDataBaseHelper New size "+mCurrentSize);
//    }
//
//    public void put(final String rawKey, final byte[] content,long systemTime){
//        long expireTime = Long.MAX_VALUE;
//        putEntry(rawKey, content.length, expireTime, systemTime);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(Column.KEY, encodedString(rawKey));
//        contentValues.put(Column.expireTime, Long.MAX_VALUE);
//        contentValues.put(Column.SIZE, content.length);
//        contentValues.put(Column.RAW_KEY, rawKey);
//        contentValues.put(Column.accessTime, systemTime);
//        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
//        mLinkedHashMapHedisDataBaseHelper.put(getTag(), rawKey, content, systemTime);
//        Log.d("FastLinkedHashMapDataBaseHelper Put "+rawKey);
//        Log.d("FastLinkedHashMapDataBaseHelper New size "+mCurrentSize);
//    }
//
//    /**
//     * Delete all out-of-date entries of current tag. May be this calling will cost much times, please
//     * invoke it careful. Make sure it will not block other io operations. It's recommend to invoke
//     * this function at low load of CPU and I/O.
//     * @param systemTime current system.
//     */
//    public void clearTrash(long systemTime){
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT %s, %s\n", Column.SIZE, Column.RAW_KEY));
//        sb.append(String.format("FROM %s\n", TABLE_NAME));
//        sb.append(String.format("WHERE %s<=%s\n",Column.expireTime, systemTime));
//
//        String sql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper clearTrash query sql " + sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return ;
//        }
//
//        do{
//            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            onEntryDeleted(rawKey, size);
//        }while(cursor.moveToNext());
//        IOUtil.safeClose(cursor);
//
//        StringBuilder deleteSB = new StringBuilder();
//        deleteSB.append(String.format("DELETE FROM %s\n", TABLE_NAME));
//        deleteSB.append(String.format("WHERE %s<=%s\n", Column.expireTime, systemTime));
//        sql = deleteSB.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper clearOutDateEntry run sql "+sql);
//        getWritableDatabase().execSQL(sql);
//        mLinkedHashMapHedisDataBaseHelper.clearTrash(getTag(), systemTime);
//    }
//
//    /**
//     * Get the specify entry and update the
//     * @param rawKey
//     * @param systemTime
//     * @return
//     */
//    public ThreeTuple<String,String,byte[]> get(String rawKey, long systemTime){
//        if(exits(rawKey, systemTime)){
//            updateAccessTime(rawKey, systemTime);
//            String md5Tag = encodedString(getTag());
//            String md5Key = encodedString(rawKey);
//            StringBuilder sb = new StringBuilder();
//            sb.append(String.format("SELECT %s\n", Column.content));
//            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
//            sb.append(String.format("WHERE %s='%s' AND %s='%s'\t", Column.TAG, md5Tag, Column.KEY, md5Key));
//            String sql = sb.toString();
//            Log.d("FastLinkedHashMapDataBaseHelper get run sql " + sql);
//            Cursor cursor = mLinkedHashMapHedisDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
//            if(null == cursor || !cursor.moveToFirst()) {
//                IOUtil.safeClose(cursor);
//                return null;
//            }
//            byte[] content = cursor.getBlob(cursor.getColumnIndex(Column.content));
//            IOUtil.safeClose(cursor);
//            if(null == content || content.length <= 0) return null;
//            if(systemTime > 0) mLinkedHashMapHedisDataBaseHelper.updateAccessTime(getTag(), rawKey, systemTime);
//            return new ThreeTuple<>(getTag(), rawKey, content);
//        }
//        return null;
//    }
//
//    private void updateAccessTime(final String rawKey, long accessTime){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(Column.KEY, encodedString(rawKey));
//        contentValues.put(Column.accessTime, accessTime);
//    }
//
//    public ThreeTuple<String,String,byte[]> eldest(long systemTime, OrderPolicy orderPolicy){
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.SIZE));
//        sb.append(String.format("FROM %s\n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\t", Column.expireTime, systemTime));
//        sb.append(String.format("ORDER BY %s\n", LinkedHashMapHedisDataBaseHelper.getQueryOrderSelection(orderPolicy)));
//        sb.append(String.format("LIMIT 1\n"));
//        String sql = sb.toString();
//        Log.d("eldest run sql "+sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql ,null);
//        if(null == cursor || !cursor.moveToFirst()){
//            clearTrash(systemTime);
//            IOUtil.safeClose(cursor);
//            return null;
//        }
//        String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//        if(ValueUtil.isEmpty(rawKey)){
//            IOUtil.safeClose(cursor);
//            return null;
//        }
//        updateAccessTime(rawKey,systemTime);
//        return mLinkedHashMapHedisDataBaseHelper.get(getTag(), rawKey, systemTime);
//    }
//
//    public List<ThreeTuple<String, String,byte[]>> getAll(long systemTime, OrderPolicy orderPolicy){
//        return mLinkedHashMapHedisDataBaseHelper.getAll(getTag(), systemTime, orderPolicy);
//    }
//
//
//    /**
//     * Get a page. This function will update the access time property.
//     * @return The list of current page contents under the specify tag,
//     */
//    public List<ThreeTuple<String, String,byte[]>> getPage(long systemTime, int pageOffset,
//                                           int count, OrderPolicy orderPolicy){
//        StringBuilder update = new StringBuilder();
//        update.append(String.format("UPDATE %s\n", TABLE_NAME));
//        update.append(String.format("SET %s=%s\n", Column.accessTime, systemTime));
//        update.append(String.format("WHERE %s>%s\t", Column.expireTime, systemTime));
//        update.append(String.format("ORDER BY %s\n", LinkedHashMapHedisDataBaseHelper.getQueryOrderSelection(orderPolicy)));
//        update.append(String.format("LIMIT %s OFFSET %s\n", count, pageOffset*count));
//        String updateSql = update.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper getPage "+updateSql);
//        getWritableDatabase().execSQL(updateSql);
//        return mLinkedHashMapHedisDataBaseHelper.getPage(getTag(), systemTime, pageOffset, count, orderPolicy);
//    }
//
//    /**
//     * Get the count of current tag. Do not change any property.
//     * @param systemTime system wall clock.
//     * @return
//     */
//    public int getCount(final long systemTime){
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT COUNT(%s) AS COUNT\n", Column.KEY));
//        sb.append(String.format("FROM %s\n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\t", Column.expireTime, systemTime));
//        String sql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper getCount run sql " + sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
//        if(null == cursor || !cursor.moveToFirst()) {
//            IOUtil.safeClose(cursor);
//            return 0;
//        }
//        Log.d("FastLinkedHashMapDataBaseHelper getCount cursor count " +cursor.getCount());
//        int count = cursor.getInt(cursor.getColumnIndex("COUNT"));
//        mCurrentCount = count;
//        IOUtil.safeClose(cursor);
//        return count;
//    }
//
//    /**
//     *
//     * @param systemTime
//     * @return
//     */
//    public int getSize( final long systemTime){
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT SUM(%s) AS SIZE\n", Column.SIZE));
//        sb.append(String.format("FROM %s\n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\t", Column.expireTime, systemTime));
//        String sql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper getSize run sql "+ sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql,null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return -1;
//        }
//        Log.d("QueueHedisDataBaseHelper getSize cursor count " +cursor.getCount());
//        int count = cursor.getInt(cursor.getColumnIndex("SIZE"));
//        mCurrentSize = count;
//        IOUtil.safeClose(cursor);
//        return count;
//    }
//
//    /**
//     * Return the list of element which include id and size.
//    * @return
//     */
//    public List<ThreeTuple<String, String,Integer>> getBasic(long systemTime, OrderPolicy orderPolicy){
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.SIZE));
//        sb.append(String.format("FROM %s\n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\t", Column.expireTime, systemTime));
//        sb.append(String.format("ORDER BY %s\n", LinkedHashMapHedisDataBaseHelper.getQueryOrderSelection(orderPolicy)));
//        String sql = sb.toString();
//        Log.d("QueueHedisDataBaseHelper.querySimple run sql "+sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return null;
//        }
//        Log.d("FastLinkedHashMapDataBaseHelper.getBasic cursor count "+cursor.getCount());
//        final List<ThreeTuple<String, String,Integer>> res = new ArrayList<>();
//        do{
//            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            ThreeTuple<String, String,Integer> element = new ThreeTuple<>(getTag(), rawKey,size);
//            res.add(element);
//        }while (cursor.moveToNext());
//        IOUtil.safeClose(cursor);
//        return res;
//    }
//
//
//    /**
//     * It's may be cause memory leak.
//     * @param maxCount
//     * @return
//     */
//    public List<ThreeTuple<String, String, byte[]>> trimCountWithResult(long systemTime,
//                                              int maxCount, OrderPolicy orderPolicy){
//        int count = mCurrentCount;
//        if(count <= maxCount){
//            Log.d("Current count "+count+" less than "+maxCount);
//            return null;
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT * \n"));
//        sb.append(String.format("FROM %s \n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
//        sb.append(String.format("ORDER BY %s\n", LinkedHashMapHedisDataBaseHelper.getQueryOrderSelection(orderPolicy)));
//        sb.append(String.format("LIMIT %s OFFSET %s\n", Integer.MAX_VALUE, maxCount));
//        String query = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper trimCountWithResult query sql " + query);
//        Cursor cursor = getReadableDatabase().rawQuery(query, null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return null;
//        }
//        Log.d("FastLinkedHashMapDataBaseHelper trimCountWithResult  query result count " +cursor.getCount());
//        do{
//            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            onEntryDeleted(rawKey, size);
//        }while(cursor.moveToNext());
//
//        cursor.moveToFirst();//第一个是临界值
//        sb.delete(0,sb.length());
//        sb.append(String.format("DELETE FROM %s \n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
//        sb.append(String.format("AND %s\n", LinkedHashMapHedisDataBaseHelper.getDeleteWhereSelection(orderPolicy, cursor)));
//        String deleteSql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper trimCountWithResult deleteSql sql " + deleteSql);
//        getWritableDatabase().execSQL(deleteSql);
//
//        return mLinkedHashMapHedisDataBaseHelper.trimCountWithResult(getTag(), systemTime, maxCount, orderPolicy);
//    }
//
//    /**
//     * Delete some items
//     * <b>The function will delete the out of date entries.</b>
//     * @param maxCount
//     * @param orderPolicy
//     */
//    public void trimCount(int maxCount, long systemTime, OrderPolicy orderPolicy){
//        int count = mCurrentCount;
//        if(count <= maxCount){
//            Log.d("Current count "+count+" less than "+maxCount);
//            return ;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT %s, %s ,%, %s\n", Column.RAW_KEY, Column.expireTime, Column.SIZE));
//        sb.append(String.format("FROM %s \n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
//        sb.append(String.format("ORDER BY %s\n", LinkedHashMapHedisDataBaseHelper.getQueryOrderSelection(orderPolicy)));
//        sb.append(String.format("LIMIT %s OFFSET %s\n",Integer.MAX_VALUE, maxCount));
//        String query = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper trimCount query sql " + query);
//        Cursor cursor = getReadableDatabase().rawQuery(query, null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return ;
//        }
//        do{
//            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            onEntryDeleted(rawKey, size);
//        }while(cursor.moveToNext());
//        IOUtil.safeClose(cursor);
//
//        sb.delete(0,sb.length());
//        sb.append(String.format("DELETE FROM %s \n", TABLE_NAME));
//        sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
//        sb.append(String.format("AND %s\n", LinkedHashMapHedisDataBaseHelper.getDeleteWhereSelection(orderPolicy, cursor)));
//        String deleteSql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper trimCount deleteSql sql " + deleteSql);
//        getWritableDatabase().execSQL(deleteSql);
//
//
//        sb.delete(0,sb.length());
//        sb.append(String.format("DELETE FROM %s \n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
//        sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, encodedString(getTag()), Column.expireTime, systemTime));
//        sb.append(String.format("AND %s\n", LinkedHashMapHedisDataBaseHelper.getDeleteWhereSelection(orderPolicy, cursor)));
//        deleteSql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper trimCount deleteSql sql " + deleteSql);
//        mLinkedHashMapHedisDataBaseHelper.getWritableDatabase().execSQL(deleteSql);
//    }
//
//    public void delete(List<String> keys) {
//        if(null == keys || keys.size() <= 0) return ;
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("SELECT %s,%s \n", Column.SIZE, Column.RAW_KEY));
//        sb.append(String.format("FROM %s \n", TABLE_NAME));
//        String selector = "";
//        for(int i = 0;i<keys.size();++i){
//            if(i>0){
//                selector = selector+",'"+ValueUtil.md5_16(keys.get(i))+"'";
//            }else{
//                selector = "'"+ValueUtil.md5_16(keys.get(i))+"'";
//            }
//        }
//        sb.append(String.format("WHERE %s IN (%s)'\n", Column.KEY, selector));
//        String query = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper deleteSilence query sql " + query);
//        Cursor cursor = getReadableDatabase().rawQuery(query, null);
//        if(null == cursor || !cursor.moveToFirst()){
//            IOUtil.safeClose(cursor);
//            return ;
//        }
//        do{
//            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            onEntryDeleted(rawKey, size);
//        }while(cursor.moveToNext());
//        IOUtil.safeClose(cursor);
//
//        //delete memory database
//        StringBuilder deleteSB = new StringBuilder();
//        deleteSB.append(String.format("DELETE FROM %s\n", TABLE_NAME));
//        deleteSB.append(String.format("WHERE %s IN (%s)\n", Column.KEY, selector));
//        String deleteSql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper deleteSilence deleteSql sql " + deleteSql);
//        getWritableDatabase().execSQL(deleteSql);
//
//        //delete from disk database
//        deleteSB = new StringBuilder();
//        deleteSB.append(String.format("DELETE FROM %s\n", TABLE_NAME));
//        deleteSB.append(String.format("WHERE %s='%s' AND %s IN (%s)\n", Column.TAG, encodedString(getTag()), Column.KEY, selector));
//        deleteSql = sb.toString();
//        Log.d("FastLinkedHashMapDataBaseHelper deleteSilence deleteSql sql " + deleteSql);
//        mLinkedHashMapHedisDataBaseHelper.getWritableDatabase().execSQL(deleteSql);
//    }
//
//    public void trimSize(int maxSize, long systemTime, OrderPolicy orderPolicy){
//        if(mCurrentSize <= maxSize) return ;
//        String sql = "SELECT "+Column.SIZE+" , "+Column.RAW_KEY +" , " + Column.accessTime+" , "+Column.expireTime + "\n"+
//                "FROM "+TABLE_NAME+"\n" +
//                "WHERE "+Column.expireTime+">"+systemTime+"\n"+
//                "ORDER BY "+LinkedHashMapHedisDataBaseHelper.getQueryOrderSelection(orderPolicy)+"\n";
//        Log.d("FastLinkedHashMapDataBaseHelper trimSize sql "+sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToLast()){
//            IOUtil.safeClose(cursor);
//            return ;
//        }
//        long deltaSize = mCurrentSize - maxSize;
//        final List<String> keyList = new ArrayList<>();
//        do{
//            String key = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            deltaSize -= size;
//            keyList.add(key);
//            onEntryDeleted(key, size);
//        }while(deltaSize > 0 && cursor.moveToPrevious());
//
//
//        if(cursor.getPosition() < 0){//delete all
//            mLinkedHashMapHedisDataBaseHelper.delete(getTag());
//        }else{//delete some
//            sql = "DELETE FROM "+LinkedHashMapHedisDataBaseHelper.TABLE_NAME+"\n" +
//                    "WHERE "+Column.TAG+"='"+encodedString(getTag())+"' AND "+Column.expireTime+">"+systemTime+" AND "+
//                    LinkedHashMapHedisDataBaseHelper.getDeleteWhereSelection(orderPolicy, cursor);
//            Log.d("FastLinkedHashMapDataBaseHelper trimSize sql "+sql);
//            mLinkedHashMapHedisDataBaseHelper.getWritableDatabase().execSQL(sql);
//        }
//        IOUtil.safeClose(cursor);
////        mLinkedHashMapHedisDataBaseHelper.deleteSilence(getTag(), keyList);
//    }
//
//
//    public List<ThreeTuple<String,String,byte[]>> trimSizeWithResult(int maxSize, long systemTime, OrderPolicy orderPolicy){
//        if(mCurrentSize <= maxSize) return null;
//        String sql = "SELECT "+Column.SIZE+" , "+Column.RAW_KEY + ", " + Column.expireTime+" , "+Column.accessTime+"\n"+
//                "FROM "+TABLE_NAME+"\n" +
//                "WHERE "+Column.expireTime+">"+systemTime+"\n"+
//                "ORDER BY "+LinkedHashMapHedisDataBaseHelper.getQueryOrderSelection(orderPolicy)+"\n";
//        Log.d("FastLinkedHashMapDataBaseHelper trimSize sql "+sql);
//        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
//        if(null == cursor || !cursor.moveToLast()){
//            IOUtil.safeClose(cursor);
//            return null;
//        }
//        long deltaSize = mCurrentSize - maxSize;
//        do{
//            String key = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
//            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
//            deltaSize -= size;
//            onEntryDeleted(key, size);
//        }while(deltaSize > 0 && cursor.moveToPrevious());
//
//        List<ThreeTuple<String,String,byte[]>>  res = null;
//
//        if(cursor.getPosition() < 0){//delete all
//            res = mLinkedHashMapHedisDataBaseHelper.getAll(getTag(), TimeUtils.getCurrentWallClockTime(), orderPolicy);
//            mLinkedHashMapHedisDataBaseHelper.delete(getTag());
//        }else{//delete some
//            sql = "SELECT " + Column.content+" , "+Column.RAW_KEY+"\n" +
//                    "FROM "+LinkedHashMapHedisDataBaseHelper.TABLE_NAME+"\n" +
//                    "WHERE "+Column.TAG+"='"+encodedString(getTag())+"' AND "+Column.expireTime+">"+systemTime+" AND "+
//                    LinkedHashMapHedisDataBaseHelper.getDeleteWhereSelection(orderPolicy, cursor);
//            Log.d("FastLinkedHashMapDataBaseHelper trimSize get all need removed sql "+sql);
//            Cursor resCursor = mLinkedHashMapHedisDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
//            if(null == resCursor || !resCursor.moveToFirst()){
//                IOUtil.safeClose(resCursor);
//                return null;
//            }
//            res = new ArrayList<>();
//            do{
//                String rawKey = resCursor.getString(resCursor.getColumnIndex(Column.RAW_KEY));
//                byte[] body = resCursor.getBlob(resCursor.getColumnIndex(Column.content));
//                ThreeTuple<String,String,byte[]> item = new ThreeTuple<>(getTag(), rawKey, body);
//                res.add(item);
//            }while(resCursor.moveToNext());
//
//            sql = "DELETE FROM "+LinkedHashMapHedisDataBaseHelper.TABLE_NAME+"\n" +
//                    "WHERE "+Column.TAG+"='"+encodedString(getTag())+"' AND "+Column.expireTime+">"+systemTime+" AND "+
//                    LinkedHashMapHedisDataBaseHelper.getDeleteWhereSelection(orderPolicy, cursor);
//            Log.d("FastLinkedHashMapDataBaseHelper trimSize real delete sql "+sql);
//            mLinkedHashMapHedisDataBaseHelper.getWritableDatabase().execSQL(sql);
//        }
//        IOUtil.safeClose(cursor);
//
//        return res;
//    }
}
