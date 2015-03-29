package com.hyn.hedis;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.LruCache;

import com.hyn.hedis.exception.HedisException;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;

import hyn.com.lib.IOUtil;
import hyn.com.lib.ThreeTuple;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.Log;

/**
 * Created by hanyanan on 2015/3/26.
 * 1. not support time restriction.
 */
public class LruSetHedis<T> implements SetHedis<T> {
    //current tag.
    private String tag;
    //remove listener ,the expire entry do not trigger this callback.
    protected OnEntryRemovedListener<T> listener;
    //Real disk database
    protected final LinkedHashMapHedisDataBaseHelper diskDataBaseHelper;
    //Object parser
    protected final ObjectParser<T> parser;
    //main lur list
    private final LinkedHashMap<String, TwoTuple<Integer,Long>> lru = new LinkedHashMap<String, TwoTuple<Integer,Long>>();
    //current size
    protected int currentSize;
    //max size
    protected int maxSize = Integer.MAX_VALUE;
    //max count
    protected int maxCount = Integer.MAX_VALUE;

    protected final HashMap<String,String> keyMap = new HashMap<>();
    public LruSetHedis(LinkedHashMapHedisDataBaseHelper linkedHashMapHedisDataBaseHelper,ObjectParser<T> parser,String tag){
        diskDataBaseHelper = linkedHashMapHedisDataBaseHelper;
        this.tag = tag;
        this.parser = parser;
        sync();
    }

    protected synchronized void onSync(){
        lru.clear();
        currentSize = 0;
    }
    //encode
    protected String parserKey(String key){
        return ValueUtil.md5_16(key);
    }

    protected synchronized void onEntryAdded(String rawKey, int size){
        currentSize += size;
        String md5 = parserKey(rawKey);
        keyMap.put(md5, rawKey);
        lru.put(md5, new TwoTuple<>(Integer.valueOf(size), TimeUtils.getCurrentWallClockTime()));
        Log.d("LruSetHedis onEntryAdded " + rawKey + "\tnew size " + currentSize + "\tnew count " + count());
    }

    protected synchronized void onEntryRemoved(String rawKey, int size){
        currentSize -= size;
        lru.remove(parserKey(rawKey));
        Log.d("LruSetHedis onEntryRemoved "+rawKey+"\tnew size "+currentSize+"\tnew count "+count());
    }

    protected synchronized void onEntryAccessed(String rawKey){
        lru.get(parserKey(rawKey));
        Log.d("LruSetHedis onEntryAccessed "+rawKey+"\tcurrent size "+currentSize+"\tnew count "+count());
    }

    protected synchronized void onEntryChanged(String rawKey, int oldSize, int newSize){
        currentSize = currentSize - oldSize + newSize;
        lru.put(parserKey(rawKey), new TwoTuple<>(Integer.valueOf(newSize), TimeUtils.getCurrentWallClockTime()));
        Log.d("LruSetHedis onEntryChanged "+rawKey+"\tnew size "+rawKey+" oldSize "+oldSize+" , newSize "+newSize);
    }

    protected synchronized Integer getSize(String rawKey){
        TwoTuple<Integer,Long> res=lru.get(parserKey(rawKey));
        if(null == res || res.firstValue == null) return -1;
        return res.firstValue.intValue();
    }

    private void log(){
        Log.d("LruSetHedis current size "+currentSize);
        Log.d("LruSetHedis current count "+count());
    }

    //Read data from disk database.
    private TwoTuple<String, byte[]> getData(String md5Key){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", BaseSQLiteOpenHelper.Column.content,BaseSQLiteOpenHelper.Column.RAW_KEY));
        sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s='%s'\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()),
                BaseSQLiteOpenHelper.Column.KEY, md5Key));
        String sql = sb.toString();
        Log.d("LruSetHedis getData "+md5Key);
        Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        byte[] data = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.content));
        String rawKey = cursor.getString(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.RAW_KEY));
        IOUtil.safeClose(cursor);
        return new TwoTuple<>(rawKey, data);
    }

    //update last access time
    private void updateAccessTime(String md5Key){
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder update = new StringBuilder();
        update.append(String.format("UPDATE %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        update.append(String.format("SET %s=%s\n", BaseSQLiteOpenHelper.Column.accessTime, systemTime));
        update.append(String.format("WHERE %s='%s' AND %s='%s'\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()),
                BaseSQLiteOpenHelper.Column.KEY, md5Key));
        String updateSql = update.toString();
        Log.d("LruSetHedis Update disk database access time "+updateSql);
        diskDataBaseHelper.getWritableDatabase().execSQL(updateSql);
    }

    private synchronized void sync(){
        onSync();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s\n", BaseSQLiteOpenHelper.Column.SIZE, BaseSQLiteOpenHelper.Column.RAW_KEY));
        sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        sb.append(String.format("WHERE %s='%s'\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag())));
        sb.append(String.format("ORDER BY %s ASC\n", BaseSQLiteOpenHelper.Column.accessTime));

        String sql = sb.toString();
        Log.d("FastSetHedisImpl sync run query sql " + sql);
        Cursor cursor = diskDataBaseHelper.getWritableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return ;
        }

        do{
            int size = cursor.getInt(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.SIZE));
            String key = cursor.getString(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.RAW_KEY));
            onEntryAdded(key,size);
        }while(cursor.moveToNext());
        IOUtil.safeClose(cursor);

        //check if out-of-restriction
        trimToSize(maxSize());
        trimToCount(maxCount());

        log();
    }

    protected synchronized boolean contain(String key){
        return lru.containsKey(parserKey(key));
    }

    @Override
    public OrderPolicy getOrderPolicy() {
        return OrderPolicy.LRU;
    }

    @Override
    public ObjectParser<T> getParser() {
        return parser;
    }

    @Override
    public ThreeTuple<String, String, T> eldest() throws HedisException {
        TwoTuple<String, Integer> entry = getEldestEntry();
        if(null == entry) return null;
        TwoTuple<String,byte[]> res = getData(entry.firstValue);
        if(null == res) return null;
        T t = getParser().transferToObject(res.secondValue);
        return new ThreeTuple<>(getTag(), res.firstValue, t);
    }

    //Return the rawKey-Size pair
    private synchronized TwoTuple<String, Integer> getEldestEntry(){
        Iterator<Map.Entry<String, TwoTuple<Integer,Long>>> iterator = lru.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, TwoTuple<Integer,Long>> entry = iterator.next();
            if(null == entry || entry.getValue() == null || entry.getValue().firstValue == null) continue;
            String rawKey = keyMap.get(entry.getKey());
            return new TwoTuple<>(rawKey, entry.getValue().firstValue);
        }
        return null;
    }
    //key-size-accessTime
    private synchronized ThreeTuple<String, Integer, Long> removeEldestEntry(){
        Iterator<Map.Entry<String, TwoTuple<Integer,Long>>> iterator = lru.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, TwoTuple<Integer,Long>> entry = iterator.next();
            if(null == entry || entry.getValue() == null || entry.getValue().firstValue == null
                    || null == entry.getValue().secondValue) continue;
            String rawKey = keyMap.get(entry.getKey());
            onEntryRemoved(rawKey, entry.getValue().firstValue);
            return new ThreeTuple<>(rawKey, entry.getValue().firstValue, entry.getValue().secondValue);
        }
        return null;
    }

    @Override
    public void put(String key, T content, long expireTimeDelta) throws HedisException {
        Log.i("LruSetHedis put ignore expireTimeDelta argument!");
        put(key, content);
    }

    @Override
    public void put(String key, T content) throws HedisException {
        byte[] body = getParser().transferToBlob(content);
        ContentValues contentValues = new ContentValues();
        long systemTime = TimeUtils.getCurrentWallClockTime();
        Integer size = getSize(key);
        if(null == size){
            onEntryAdded(key, body.length);
        }else{
            onEntryChanged(key, size.intValue(), body.length);
        }
        contentValues.put(BaseSQLiteOpenHelper.Column.modifyTime, systemTime);
        contentValues.put(BaseSQLiteOpenHelper.Column.RAW_KEY, key);
        contentValues.put(BaseSQLiteOpenHelper.Column.expireTime, Long.MAX_VALUE);
        contentValues.put(BaseSQLiteOpenHelper.Column.KEY, parserKey(key));
        contentValues.put(BaseSQLiteOpenHelper.Column.SIZE, body.length);
        contentValues.put(BaseSQLiteOpenHelper.Column.accessTime, systemTime);
        contentValues.put(BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()));
        contentValues.put(BaseSQLiteOpenHelper.Column.content, body);
        //put to disk database.
        diskDataBaseHelper.getWritableDatabase().insertWithOnConflict(
                diskDataBaseHelper.TABLE_NAME, null,
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public T replace(String key, T content, long expireTimeDelta) throws HedisException {
        ThreeTuple<String, String, T> res = get(key);
        put(key, content, expireTimeDelta);
        if(null == res) return null;
        return res.thirdValue;
    }

    @Override
    public T replace(String key, T content) throws HedisException {
        ThreeTuple<String, String, T> res = get(key);
        put(key, content);
        if(null == res) return null;
        return res.thirdValue;
    }

    @Override
    public List<ThreeTuple<String, String, T>> getAll() throws HedisException {
        List<ThreeTuple<String, String, T>> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", BaseSQLiteOpenHelper.Column.RAW_KEY, BaseSQLiteOpenHelper.Column.content));
        sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        sb.append(String.format("WHERE %s='%s'\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag())));
        sb.append(String.format("ORDER BY %s DESC\n", BaseSQLiteOpenHelper.Column.accessTime));
        String sql = sb.toString();
        Log.d("FastSetHedisImpl getAll run sql " + sql);
        Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d("LruSetHedis getAll cursor count "+cursor.getCount());
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.content));
            String rawKey = cursor.getString(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.RAW_KEY));
            ThreeTuple<String, String, T> element = new ThreeTuple<>(getTag(), rawKey,getParser().transferToObject(body));
            res.add(element);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    @Override
    public List<ThreeTuple<String, String, T>> getPage(int pageIndex, int pageCount) throws HedisException {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        final List<ThreeTuple<String, String, T>> res = new ArrayList<>();

        {//get real content from disk database
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s,%s\n", BaseSQLiteOpenHelper.Column.RAW_KEY, BaseSQLiteOpenHelper.Column.content));
            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s'\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag())));
            sb.append(String.format("ORDER BY %s ASC\n", BaseSQLiteOpenHelper.Column.accessTime));
            sb.append(String.format("LIMIT %s, %s\n", pageIndex * pageCount, pageIndex * pageCount + pageCount));
            String sql = sb.toString();
            Log.d("LruSetHedis getPage query real data run sql " + sql);
            Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()){
                IOUtil.safeClose(cursor);
                return null;
            }
            Log.d("LruSetHedis getPage get count "+cursor.getCount());
            do{
                byte[] body = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.content));
                String rawKey = cursor.getString(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.RAW_KEY));
                ThreeTuple<String, String, T>element = new ThreeTuple<>(tag, rawKey,getParser().transferToObject(body));
                res.add(element);
                onEntryAccessed(rawKey);
            }while (cursor.moveToNext());
            IOUtil.safeClose(cursor);
        }

        {//update access time property.
            StringBuilder update = new StringBuilder();
            update.append(String.format("UPDATE %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            update.append(String.format("SET %s=%s\n", BaseSQLiteOpenHelper.Column.accessTime, systemTime));
            if (enableExpireTime()) {
                update.append(String.format("WHERE %s='%s' AND %s>%s AND %s IN(\n", BaseSQLiteOpenHelper.Column.TAG,
                        parserKey(getTag()), BaseSQLiteOpenHelper.Column.expireTime, systemTime, BaseSQLiteOpenHelper.Column.KEY));
            }else{
                update.append(String.format("WHERE %s='%s' AND %s IN(\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()), BaseSQLiteOpenHelper.Column.KEY));
            }
            update.append(String.format("SELECT %s\n", BaseSQLiteOpenHelper.Column.KEY));
            update.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            update.append(String.format("ORDER BY %s ASC\n", BaseSQLiteOpenHelper.Column.accessTime));
            update.append(String.format("LIMIT %s, %s\n", pageIndex * pageCount, pageIndex * pageCount + pageCount));
            update.append(")\n");

            String updateSql = update.toString();
            Log.d("LruSetHedis Update disk database access time "+updateSql);
            diskDataBaseHelper.getWritableDatabase().execSQL(updateSql);
        }

        return res;
    }

    @Override
    public ThreeTuple<String, String, T> get(String key) throws HedisException {
        return doGet(key, true);
    }

    private ThreeTuple<String, String, T> doGet(String key, boolean updateAccessTime) throws HedisException {
        Integer size = getSize(key);
        if(null == size){
            return null;
        }

        ThreeTuple<String, String, T> res = null;

        {//query from disk database
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s\n", BaseSQLiteOpenHelper.Column.content));
            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s='%s'\t", BaseSQLiteOpenHelper.Column.TAG,
                    parserKey(getTag()), BaseSQLiteOpenHelper.Column.KEY, parserKey(key)));
            String sql = sb.toString();
            Log.d("LruSetHedis get run sql " + sql);
            Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()) {// not exist
                IOUtil.safeClose(cursor);
                return null;
            }

            byte[] content = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.content));
            IOUtil.safeClose(cursor);
            if(null != content && content.length > 0) {
                res = new ThreeTuple<>(getTag(), key, getParser().transferToObject(content));
            }
        }

        if(updateAccessTime) {//update disk database property
            updateAccessTime(parserKey(key));
        }
        return res;
    }

    @Override
    public synchronized void remove(String key) {
        Integer size = getSize(key);
        if(null == size) return ;
        onEntryRemoved(key, size.intValue());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s='%s'\n", BaseSQLiteOpenHelper.Column.TAG,
                                parserKey(getTag()), BaseSQLiteOpenHelper.Column.KEY, parserKey(key)));
        String sql = sb.toString();
        Log.d("LruSetHedis remove run sql " + sql);
        diskDataBaseHelper.getWritableDatabase().execSQL(sql);
    }

    @Override
    public ThreeTuple<String, String, T> fetch(String key) throws HedisException {
        Integer size = getSize(key);
        if(null == size) return null;
        ThreeTuple<String, String, T> res = doGet(key, false);
        remove(key);
        return res;
    }

    @Override
    public synchronized void evictAll() {
        onSync();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        sb.append(String.format("WHERE %s='%s'\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag())));
        String sql = sb.toString();
        Log.d("LruSetHedis evictAll run sql " + sql);
        diskDataBaseHelper.getWritableDatabase().execSQL(sql);
    }

    @Override
    public int size() {
        return currentSize;
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public int count() {
        return lru.size();
    }

    @Override
    public int maxCount() {
        return maxCount;
    }

    private void notifyEntryRemoved(String key, byte[] content){
        if(null != listener){
            try {
                listener.onEntryRemoved(getTag(), key, getParser().transferToObject(content));
            } catch (HedisException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public synchronized void trimToCount(int maxRowCount) {
        maxCount = maxRowCount;
        if(count() <= maxRowCount) return ;
        ThreeTuple<String, Integer, Long> recentlyEntry = null;
        while(count() >= maxRowCount){
            synchronized (this) {
                if (size() < 0 || (lru.isEmpty() && size() != 0)) {
                    throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }
                recentlyEntry = removeEldestEntry();
            }
        }
        if(recentlyEntry == null){
            return ;
        }

        if(null != listener){
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s, %s\n", BaseSQLiteOpenHelper.Column.RAW_KEY, BaseSQLiteOpenHelper.Column.content));
            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s<=%s\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()),
                    BaseSQLiteOpenHelper.Column.accessTime, recentlyEntry.thirdValue.longValue()));
            String sql = sb.toString();
            Log.d("LruSetHedis trimToCount run sql " + sql);
            Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()){//something bad happend.
                IOUtil.safeClose(cursor);
                sync();
                return ;
            }

            do{
                String rawKey = cursor.getString(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.RAW_KEY));
                byte[] data = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.content));
                notifyEntryRemoved(rawKey,data);
            }while(cursor.moveToNext());
        }

        {//delete from disk
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s<=%s\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()),
                    BaseSQLiteOpenHelper.Column.accessTime, recentlyEntry.thirdValue.longValue()));
            String sql = sb.toString();
            Log.d("LruSetHedis trimToCount run sql " + sql);
            diskDataBaseHelper.getWritableDatabase().execSQL(sql);
        }
    }

    @Override
    public void trimToSize(int maxSize) {
        this.maxSize = maxSize;
        if(size() <= maxSize) return ;
        ThreeTuple<String, Integer, Long> recentlyEntry = null;
        while(size() >= maxSize){
            synchronized (this) {
                if (size() < 0 || (lru.isEmpty() && size() != 0)) {
                    throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }
                recentlyEntry = removeEldestEntry();
            }
        }
        if(recentlyEntry == null){
            return ;
        }

        if(null != listener){
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s, %s\n", BaseSQLiteOpenHelper.Column.RAW_KEY, BaseSQLiteOpenHelper.Column.content));
            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s<=%s\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()),
                    BaseSQLiteOpenHelper.Column.accessTime, recentlyEntry.thirdValue.longValue()));
            String sql = sb.toString();
            Log.d("LruSetHedis trimToCount run sql " + sql);
            Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()){//something bad happend.
                IOUtil.safeClose(cursor);
                sync();
                return ;
            }

            do{
                String rawKey = cursor.getString(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.RAW_KEY));
                byte[] data = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.content));
                notifyEntryRemoved(rawKey,data);
            }while(cursor.moveToNext());
        }

        {//delete from disk
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s<=%s\n", BaseSQLiteOpenHelper.Column.TAG, parserKey(getTag()),
                    BaseSQLiteOpenHelper.Column.accessTime, recentlyEntry.thirdValue.longValue()));
            String sql = sb.toString();
            Log.d("LruSetHedis trimToCount run sql " + sql);
            diskDataBaseHelper.getWritableDatabase().execSQL(sql);
        }
    }

    @Override
    protected final synchronized void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public boolean enableExpireTime() {
        return false;
    }

    @Override
    public void setRemoveListener(OnEntryRemovedListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public synchronized void dispose() {
        diskDataBaseHelper.close();
        lru.clear();
        listener = null;
        currentSize = 0;
        Log.d("LruSetHedis dispose!");
    }
}
