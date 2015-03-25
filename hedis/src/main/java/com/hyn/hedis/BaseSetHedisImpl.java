package com.hyn.hedis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hyn.hedis.exception.HedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import hyn.com.lib.IOUtil;
import hyn.com.lib.ThreeTuple;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.Log;

import com.hyn.hedis.BaseSQLiteOpenHelper.Column;
/**
 * Created by hanyanan on 2015/3/16.
 */
public class BaseSetHedisImpl<T> implements SetHedis<T> {
    protected final ObjectParser<T> objectParser;
    protected final OrderPolicy orderPolicy;
    //current all size, include both expired time entry and invalid entries
    protected int currentSize = 0;
    //current all size, include both expired time entry and invalid entries
    protected int currentCount = 0;
    //the max size of the current tag, if it's bigger than current value, than delete from both
    // expired entries and the tail of the current.
    protected int maxSize = Integer.MAX_VALUE;
    //the max size of the current tag, if it's bigger than current value, than delete from both
    // expired entries and the tail of the current.
    protected int maxCount = Integer.MAX_VALUE;

    //A memory database to build the index to increase the effective.
    protected final FastLinkedHashMapDataBaseHelper memoryIndexDataBaseHelper;
    //The real disk database that store the data.
    protected final LinkedHashMapHedisDataBaseHelper diskDataBaseHelper;
    //The remove listener.
    protected OnEntryRemovedListener<T> removedListener;
    //current tag
    protected final String tag;
    //the last
    protected long lastClearTimeTag = -1;

    protected final boolean supportTimeRestrict;
    public BaseSetHedisImpl(Context context, String tag, LinkedHashMapHedisDataBaseHelper hashMapHedisDataBaseHelper,
                            ObjectParser objectParser, OrderPolicy orderPolicyO, boolean supportTimeRestrict){
        memoryIndexDataBaseHelper = new FastLinkedHashMapDataBaseHelper(context);
        diskDataBaseHelper = hashMapHedisDataBaseHelper;
        this.orderPolicy = orderPolicyO;
        this.objectParser = objectParser;
        this.tag = tag;
        this.supportTimeRestrict = supportTimeRestrict;
        sync();
    }

    public BaseSetHedisImpl(String tag, FastLinkedHashMapDataBaseHelper memoryIndexDataBaseHelper,
                            LinkedHashMapHedisDataBaseHelper hashMapHedisDataBaseHelper,
                            ObjectParser objectParser, OrderPolicy orderPolicyO, boolean supportTimeRestrict){
        this.memoryIndexDataBaseHelper = memoryIndexDataBaseHelper;
        diskDataBaseHelper = hashMapHedisDataBaseHelper;
        this.orderPolicy = orderPolicyO;
        this.objectParser = objectParser;
        this.tag = tag;
        this.supportTimeRestrict = supportTimeRestrict;
        sync();
    }

    public void setRemoveListener(OnEntryRemovedListener listener){
        this.removedListener = listener;
    }
    @Override
    public final OrderPolicy getOrderPolicy() {
        return orderPolicy;
    }

    @Override
    public final ObjectParser<T> getParser() {
        return objectParser;
    }

    //encode
    protected String parserKey(String key){
        return ValueUtil.md5_16(key);
    }

    protected synchronized void onEntryAdded(String key, int size){
        currentSize += size;
        currentCount ++;
        Log.d("FastSetHedisImpl onEntryAdded "+key+"\tnew size "+currentSize+"\tnew count "+currentCount);
    }

    protected synchronized void onEntryRemoved(String key, int size){
        currentSize -= size;
        currentCount --;
        Log.d("FastSetHedisImpl onEntryRemoved "+key+"\tnew size "+currentSize+"\tnew count "+currentCount);
    }

    protected synchronized void onEntryChanged(String key, int oldSize, int newSize){
        currentSize = currentSize - oldSize + newSize;
        Log.d("FastSetHedisImpl onEntryChanged "+key+"\tnew size "+key+" oldSize "+oldSize+" , newSize "+newSize);
    }

    public synchronized void onReSynchronize(){
        currentSize = 0;
        currentCount = 0;
    }
    //check if time valid.
    private void checkTime(long expireTime, long systemTime){
        if(systemTime > expireTime) throw new IllegalArgumentException("expire time cannot before than current time.");
    }

    /**
     * sync the data from with disk database
     */
    private synchronized void sync(){
        String clear = "DELETE FROM "+FastLinkedHashMapDataBaseHelper.TABLE_NAME;
        memoryIndexDataBaseHelper.getWritableDatabase().execSQL(clear);
        onReSynchronize();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s, %s, %s\n", Column.SIZE, Column.expireTime, Column.accessTime,Column.RAW_KEY));
        sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        if(enableExpireTime()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n",Column.TAG, parserKey(getTag()), Column.expireTime, TimeUtils.getCurrentWallClockTime()));
        }else{
            sb.append(String.format("WHERE %s='%s'\n",Column.TAG, parserKey(getTag())));
        }
        String sql = sb.toString();
        Log.d("FastSetHedisImpl sync run query sql " + sql);
        Cursor cursor = diskDataBaseHelper.getWritableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return ;
        }

        do{
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            Long exp = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
            Long acc = cursor.getLong(cursor.getColumnIndex(Column.accessTime));
            String key = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.KEY, parserKey(key));
            contentValues.put(Column.RAW_KEY, key);
            contentValues.put(Column.expireTime, exp);
            contentValues.put(Column.SIZE, size);
            contentValues.put(Column.accessTime, acc);
            memoryIndexDataBaseHelper.getWritableDatabase().insertWithOnConflict(
                                    memoryIndexDataBaseHelper.TABLE_NAME, null, contentValues,
                                    SQLiteDatabase.CONFLICT_REPLACE);
            onEntryAdded(key,size);
        }while(cursor.moveToNext());
        IOUtil.safeClose(cursor);

        //check if out-of-restriction
        trimToSize(maxSize());
        trimToCount(maxCount());
        Log.d("FastSetHedisImpl sync size "+currentSize);
        Log.d("FastSetHedisImpl sync count "+currentCount);
    }

    /**
     * Update last access time for the specify entry.
     * @param rawKey the specify key
     * @param accessTime
     */
    private void updateAccessTime(final String rawKey, long accessTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.KEY, parserKey(rawKey));
        contentValues.put(Column.accessTime, accessTime);
        memoryIndexDataBaseHelper.getWritableDatabase().insertWithOnConflict(
                memoryIndexDataBaseHelper.TABLE_NAME, null, contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Check if contain the key, it need to check if current item out of date, if it true then need
     * delete from both current memory dataBase and disk dataBase.
     * @param rawKey
     * @return
     */
    private int getSize(String rawKey){
        String key = parserKey(rawKey);
        StringBuilder sb = new StringBuilder();
        if(enableExpireTime()){
            long systemTime = TimeUtils.getCurrentWallClockTime();
            sb.append(String.format("SELECT %s, %s\n", Column.SIZE, Column.expireTime));
            sb.append(String.format("FROM %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s'\t",Column.KEY, key));
            String sql = sb.toString();
            Log.d("FastSetHedisImpl getSize run sql "+sql);
            Cursor cursor = memoryIndexDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()) {
                IOUtil.safeClose(cursor);
                return -1;
            }
            Long expire = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            if(null == expire || (systemTime > 0 && expire <= systemTime) || size <= 0){
                IOUtil.safeClose(cursor);
                doRemove(rawKey);
                return -1;
            }
            IOUtil.safeClose(cursor);
            return size;
        }else{
            sb.append(String.format("SELECT %s\n", Column.SIZE));
            sb.append(String.format("FROM %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s'\t",Column.KEY, key));
            String sql = sb.toString();
            Log.d("FastSetHedisImpl getSize run sql "+sql);
            Cursor cursor = memoryIndexDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()) {
                IOUtil.safeClose(cursor);
                return -1;
            }
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            IOUtil.safeClose(cursor);
            return size;
        }
    }

    /**
     * The minimum time interval
     * @return
     */
    private int minimumClearInterval(){
        return 1000 * 10;
    }

    /**
     * Delete all out-of-date entries of current tag. May be this calling will cost much times, please
     * invoke it careful. Make sure it will not block other io operations. It's recommend to invoke
     * this function at low load of CPU and I/O.
     * The minimum time interval
     */
    public void clearTrash(){
        if(!enableExpireTime()) return ;
        long systemTime = TimeUtils.getCurrentWallClockTime();
        if(systemTime - lastClearTimeTag < minimumClearInterval()){
            Log.d("clearTrash ignore current operation!");
            return;
        }

        lastClearTimeTag = systemTime;
        {//check if has the expired entry, and delete from memory database.
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s, %s\n", Column.SIZE, Column.RAW_KEY));
            sb.append(String.format("FROM %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s<=%s\n", Column.expireTime, systemTime));

            String sql = sb.toString();
            Log.d("FastSetHedisImpl clearTrash query sql " + sql);
            Cursor cursor = memoryIndexDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if (null == cursor || !cursor.moveToFirst()) {
                IOUtil.safeClose(cursor);
                return;
            }

            do {
                String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
                int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
                onEntryRemoved(rawKey, size);
            } while (cursor.moveToNext());
            IOUtil.safeClose(cursor);
        }

        {//delete the expired entry from memory database
            StringBuilder deleteSB = new StringBuilder();
            deleteSB.append(String.format("DELETE FROM %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            deleteSB.append(String.format("WHERE %s<=%s\n", Column.expireTime, systemTime));
            String sql = deleteSB.toString();
            Log.d("FastSetHedisImpl clearTrash from memory database run sql " + sql);
            memoryIndexDataBaseHelper.getWritableDatabase().execSQL(sql);
        }

        {//remove the expired entry from disk database.
            StringBuilder deleteSB = new StringBuilder();
            deleteSB.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            deleteSB.append(String.format("WHERE %s='%s' AND %s<=%s\n", Column.TAG, parserKey(getTag()), Column.expireTime, systemTime));
            String sql = deleteSB.toString();
            Log.d("FastSetHedisImpl clearTrash from disk database run sql " + sql);
            diskDataBaseHelper.getWritableDatabase().execSQL(sql);
        }
    }

    @Override
    public ThreeTuple<String, String, T> eldest() throws HedisException {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.RAW_KEY));
        sb.append(String.format("FROM %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
        if(enableExpireTime()) {
            sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
        }
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(getOrderPolicy())));
        sb.append(String.format("LIMIT 1\n"));
        String sql = sb.toString();
        Log.d("FastSetHedisImpl to get eldest to run sql "+sql);

        //query the eldest entry.
        Cursor cursor = memoryIndexDataBaseHelper.getReadableDatabase().rawQuery(sql ,null);
        if(null == cursor || !cursor.moveToFirst()){//no entry currently. something error happened..
            clearTrash();
            IOUtil.safeClose(cursor);
            return null;
        }
        String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));//key of eldest entry
        if(ValueUtil.isEmpty(rawKey)){//no entry currently.
            clearTrash();
            IOUtil.safeClose(cursor);
            return null;
        }
        TwoTuple<String, byte[]> res = doGet(rawKey, false);//get the eldest entry. Ignore the time restrict.
        if(null == res || res.secondValue == null || res.secondValue.length <=0) return null;
        return new ThreeTuple(getTag(), rawKey, getParser().transferToObject(res.secondValue));
    }

     private void putEntry(String rawKey, int newLength){
        int prevSize = getSize(rawKey);
        if(prevSize > 0){
            onEntryChanged(rawKey, prevSize, newLength);
        }else{
            onEntryAdded(rawKey, newLength);
        }
    }

    private synchronized void trim(){
        if(enableExpireTime()){
            clearTrash();
        }
        trimToCount(maxCount());
        trimToSize(maxSize());
    }
    @Override
    public void put(String key, T content, long expireTimeDelta) throws HedisException {
        long expireTime = Integer.MAX_VALUE - 1;
        long systemTime = TimeUtils.getCurrentWallClockTime();

        ContentValues contentValues = new ContentValues();
        if(enableExpireTime()){
            expireTime = expireTimeDelta + systemTime;
            checkTime(expireTime, systemTime);
            contentValues.put(Column.expireTime, expireTime);
        }else{
            contentValues.put(Column.expireTime, Long.MAX_VALUE);
        }
        byte[] body = getParser().transferToBlob(content);
        if(body == null || body.length <= 0){//do remove operation
            doRemove(key);
            return ;
        }
        putEntry(key, body.length);
        contentValues.put(Column.KEY, parserKey(key));
        contentValues.put(Column.RAW_KEY, key);
        contentValues.put(Column.SIZE, body.length);
        contentValues.put(Column.accessTime, systemTime);
        //put to memory database.
        memoryIndexDataBaseHelper.getWritableDatabase().insertWithOnConflict(
                memoryIndexDataBaseHelper.TABLE_NAME, null,
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        contentValues.put(Column.TAG, parserKey(getTag()));
        contentValues.put(Column.content, body);
        contentValues.put(Column.modifyTime, systemTime);

        //put to disk database.
        diskDataBaseHelper.getWritableDatabase().insertWithOnConflict(
                diskDataBaseHelper.TABLE_NAME, null,
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        trim();//check the size or count.

        Log.d("FastSetHedisImpl Put "+key);
        Log.d("FastSetHedisImpl New size "+ currentSize);
    }

    @Override
    public void put(String key, T content) throws HedisException {
        put(key, content, enableExpireTime()?Long.MAX_VALUE - TimeUtils.getCurrentWallClockTime() - 1000 * 60 * 60 * 30:-1);
    }

    @Override
    public T replace(String key, T content, long expireTimeDelta) throws HedisException {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        ContentValues contentValues = new ContentValues();
        if(enableExpireTime()){
            long expireTime = expireTimeDelta + systemTime;
            checkTime(expireTime, systemTime);
            contentValues.put(Column.expireTime, expireTime);
        }else{
            contentValues.put(Column.expireTime, Long.MAX_VALUE);
        }
        byte[] body = getParser().transferToBlob(content);
        TwoTuple<String, byte[]> res = doGet(key, enableExpireTime());//TODO
        if(null != res && null != res.secondValue && res.secondValue.length>0){
            onEntryChanged(key, res.secondValue.length, body.length);
        }else{
            onEntryAdded(key, body.length);
        }

        contentValues.put(Column.KEY, parserKey(key));
        contentValues.put(Column.RAW_KEY, key);
        contentValues.put(Column.SIZE, body.length);
        contentValues.put(Column.accessTime, systemTime);
        //put to memory database.
        memoryIndexDataBaseHelper.getWritableDatabase().insertWithOnConflict(
                                        memoryIndexDataBaseHelper.TABLE_NAME, null,
                                        contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        contentValues.put(Column.TAG, parserKey(getTag()));
        contentValues.put(Column.content, body);
        contentValues.put(Column.modifyTime, systemTime);
        //put to disk database.
        diskDataBaseHelper.getWritableDatabase().insertWithOnConflict(
                                        diskDataBaseHelper.TABLE_NAME, null,
                                        contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        Log.d("FastSetHedisImpl Put "+key);
        Log.d("FastSetHedisImpl New size "+ currentSize);

        trim();

        if(null == res || null == res.secondValue || res.secondValue.length <=0) return null;
        return getParser().transferToObject(res.secondValue);
    }

    @Override
    public T replace(String key, T content) throws HedisException {
        return replace(key,content,Long.MAX_VALUE - TimeUtils.getCurrentWallClockTime() - 1000 * 60 * 60 * 30);
    }

    @Override
    public List<ThreeTuple<String, String, T>> getAll() throws HedisException {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.content));
        sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
        if(enableExpireTime()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, parserKey(getTag()), Column.expireTime, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.TAG, parserKey(getTag())));
        }
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));

        String sql = sb.toString();
        Log.d("FastSetHedisImpl getAll run sql " + sql);
        Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d("FastSetHedisImpl getAll cursor count "+cursor.getCount());
        final List<ThreeTuple<String, String, T>> res = new ArrayList<>();
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            ThreeTuple<String, String, T> element = new ThreeTuple<>(tag, rawKey,getParser().transferToObject(body));
            res.add(element);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    @Override
    public List<ThreeTuple<String, String, T>> getPage(int pageIndex, int pageCount) throws HedisException {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        final List<ThreeTuple<String, String, T>> res = new ArrayList<>();
        {//update memory info
            StringBuilder update = new StringBuilder();
            update.append(String.format("UPDATE %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            update.append(String.format("SET %s=%s\n", Column.accessTime, systemTime));
            if (enableExpireTime()) {
                update.append(String.format("WHERE %s>%s AND %s In ( \n", Column.expireTime, systemTime, Column.KEY));
            }else{
                update.append(String.format("WHERE %s In (\n", Column.KEY));
            }
            update.append(String.format("SELECT %s\n", Column.KEY));
            update.append(String.format("FROM %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            update.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
            update.append(String.format("LIMIT %s, %s\n", pageIndex * pageCount, pageIndex * pageCount + pageCount));
            update.append("\t)\n");
            String updateSql = update.toString();
            Log.d("FastSetHedisImpl getPage Update memory database by run sql " + updateSql);
            memoryIndexDataBaseHelper.getWritableDatabase().execSQL(updateSql);
        }

        {//get real content from disk database
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.content));
            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            if (enableExpireTime()) {
                sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, parserKey(getTag()), Column.expireTime, systemTime));
            }else{
                sb.append(String.format("WHERE %s='%s'\n", Column.TAG, parserKey(getTag())));
            }
            sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
            sb.append(String.format("LIMIT %s, %s\n", pageIndex * pageCount, pageIndex * pageCount + pageCount));
            String sql = sb.toString();
            Log.d("FastSetHedisImpl getPage query real data run sql " + sql);
            Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()){
                IOUtil.safeClose(cursor);
                return null;
            }
            Log.d("FastSetHedisImpl getPage get count "+cursor.getCount());
            do{
                byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.content));
                String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
                ThreeTuple<String, String, T>element = new ThreeTuple<>(tag, rawKey,getParser().transferToObject(body));
                res.add(element);
            }while (cursor.moveToNext());
            IOUtil.safeClose(cursor);
        }

        {//update access time property.
            StringBuilder update = new StringBuilder();
            update.append(String.format("UPDATE %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            update.append(String.format("SET %s=%s\n", Column.accessTime, systemTime));
            if (enableExpireTime()) {
                update.append(String.format("WHERE %s='%s' AND %s>%s AND %s IN(\n", Column.TAG,
                        parserKey(getTag()), Column.expireTime, systemTime, Column.KEY));
            }else{
                update.append(String.format("WHERE %s='%s' AND %s IN(\n", Column.TAG, parserKey(getTag()), Column.KEY));
            }
            update.append(String.format("SELECT %s\n", Column.KEY));
            update.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            update.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
            update.append(String.format("LIMIT %s, %s\n", pageIndex * pageCount, pageIndex * pageCount + pageCount));
            update.append(")\n");

            String updateSql = update.toString();
            Log.d("FastSetHedisImpl Update disk database access time "+updateSql);
            diskDataBaseHelper.getWritableDatabase().execSQL(updateSql);
        }

        return res;
    }

    @Override
    public ThreeTuple<String, String, T> get(String key) throws HedisException {
        TwoTuple<String, byte[]> res = doGet(key, enableExpireTime());
        if(null == res || res.secondValue == null || res.secondValue.length <= 0) return null;
        return new ThreeTuple<>(getTag(), key, getParser().transferToObject(res.secondValue));
    }

    //Get the content and update access time.
    private TwoTuple<String, byte[]> doGet(String key, boolean enableTimeRestrict) throws HedisException {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        if(getSize(key) <= 0){
            return null;
        }
        TwoTuple<String, byte[]> res = null;

        {//query from disk database
            StringBuilder sb = new StringBuilder();
            if(enableExpireTime()) {
                sb.append(String.format("SELECT %s,%s\n", Column.expireTime, Column.content));
            }else{
                sb.append(String.format("SELECT %s\n", Column.content));
            }
            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s='%s'\t", Column.TAG, parserKey(getTag()), Column.KEY, parserKey(key)));
            if(enableExpireTime()) {
                sb.append(String.format("AND %s>%s\n", Column.expireTime, systemTime));
            }
            String sql = sb.toString();
            Log.d("FastSetHedisImpl get run sql " + sql);
            Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()) {// not exist
                IOUtil.safeClose(cursor);
                return null;
            }

            if(enableExpireTime()) {//check expire time
                Long time = cursor.getLong(cursor.getColumnIndex(Column.expireTime));
                if(null == time){//some thing bad has happened
                    sync();
                    IOUtil.safeClose(cursor);
                    return null;
                }
                if(time <= systemTime){//expired, remove it
                    doRemove(key);
                    return null;
                }
            }

            byte[] content = cursor.getBlob(cursor.getColumnIndex(Column.content));
            IOUtil.safeClose(cursor);
            if(null != content && content.length > 0) {
                res = new TwoTuple<>(key, content);
            }
        }

        {//update disk database property
            updateAccessTime(key, systemTime);
        }

        return res;
    }

    @Override
    public void remove(String key) {
        doRemove(key);
    }

    @Override
    public ThreeTuple<String, String, T> fetch(String key) throws HedisException {
        ThreeTuple<String, String, T> res = get(key);
        doRemove(key);
        return res;
    }

    public void doRemove(String key) {
        int size;
        {//Get size, 忽略时间的作用，无论是否过时，都需要删除
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s\n", Column.SIZE));
            sb.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, parserKey(key)));
            String sql = sb.toString();
            Log.d("FastSetHedisImpl get size memory index run sql " + sql);
            Cursor cursor = memoryIndexDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
            if(null == cursor || !cursor.moveToFirst()) {// not exist
                IOUtil.safeClose(cursor);
                return ;
            }
            size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            IOUtil.safeClose(cursor);
        }

        if(size <= 0) return;

        {//delete index from memory database
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, parserKey(key)));
            String sql = sb.toString();
            Log.d("FastSetHedisImpl remove memory index run sql " + sql);
            memoryIndexDataBaseHelper.getWritableDatabase().execSQL(sql);
        }

        onEntryRemoved(key, size);

        {//delete from disk database
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s='%s'\n", Column.TAG, parserKey(getTag()), Column.KEY, parserKey(key)));
            String sql = sb.toString();
            Log.d("FastSetHedisImpl remove disk database run sql " + sql);
            diskDataBaseHelper.getWritableDatabase().execSQL(sql);
        }
    }

    @Override
    public synchronized void evictAll() {
        String clear = "DELETE FROM "+FastLinkedHashMapDataBaseHelper.TABLE_NAME;
        memoryIndexDataBaseHelper.getWritableDatabase().execSQL(clear);
        onReSynchronize();

        clear = "DELETE FROM "+LinkedHashMapHedisDataBaseHelper.TABLE_NAME+"\n"+
                "WHERE "+Column.TAG+"='"+parserKey(getTag())+"'\n";
        diskDataBaseHelper.getWritableDatabase().execSQL(clear);
    }

    @Override
    public final synchronized int size() {
        if(enableExpireTime()) clearTrash();
        return currentSize;
    }

    @Override
    public final synchronized int maxSize() {
        return maxSize;
    }

    @Override
    public final synchronized int count() {
        if(enableExpireTime()) clearTrash();
        return currentCount;
    }

    @Override
    public final synchronized  int maxCount() {
        return maxCount;
    }

    private void notifyRemoveListener(String key, byte[] content){
        if(null != removedListener){
            try {
                removedListener.onEntryRemoved(getTag(), key, getParser().transferToObject(content));
            } catch (HedisException e) {
                e.printStackTrace();
                removedListener.onEntryRemoved(getTag(), key, null);
            }
        }
    }
    private void notifyRemoveListener(List<ThreeTuple<String,String,T>> out){
        if(null == out || out.size() <= 0 || removedListener == null) return ;
        for(ThreeTuple<String,String,T> tuple : out){
            if(null == tuple) continue;
              removedListener.onEntryRemoved(tuple.firstValue,tuple.secondValue, tuple.thirdValue);
        }
    }
    @Override
    public synchronized void trimToCount(int maxRowCount) {
        maxCount = maxRowCount;
        if(enableExpireTime()) clearTrash();
        if(currentCount <= maxCount){
            Log.d("FastSetHedisImpl trimToCount, Current count "+currentCount+" less than "+maxCount);
            return ;
        }

        long systemTime = TimeUtils.getCurrentWallClockTime();
        String deleteSelection = "1=1";
        {//get the will deleted entries.
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s, %s, %s, %s,%s\n", Column.RAW_KEY, Column.SIZE, Column.SIZE,Column.accessTime, Column.expireTime));
            sb.append(String.format("FROM %s \n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            if (enableExpireTime()) {
                sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
            }
            sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(getOrderPolicy())));
            sb.append(String.format("LIMIT %s, %s\n", maxCount, maxCount+1000000));
            String query = sb.toString();
            Log.d("FastSetHedisImpl trimCount query sql " + query);
            Cursor cursor = memoryIndexDataBaseHelper.getReadableDatabase().rawQuery(query, null);
            if (null == cursor || !cursor.moveToFirst()) {//delete none? some thing bad happens! retry again.
                IOUtil.safeClose(cursor);
                sync();
                return;
            }

            do {
                String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
                int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
                onEntryRemoved(rawKey, size);
            } while (cursor.moveToNext());
            cursor.moveToFirst();
            deleteSelection = getDeleteWhereSelection(getOrderPolicy(), cursor);
            IOUtil.safeClose(cursor);

            //delete tail entries from memory database
            StringBuilder sb1 = new StringBuilder();
            sb1.append(String.format("DELETE FROM %s \n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            if (enableExpireTime()) {
                sb1.append(String.format("WHERE %s>%s AND %s\n", Column.expireTime, systemTime, deleteSelection));
            }else{
                sb1.append(String.format("WHERE %s\n", deleteSelection));
            }
            String deleteSql = sb1.toString();
            Log.d("FastSetHedisImpl trimCount delete memory database run sql " + deleteSql);
            memoryIndexDataBaseHelper.getWritableDatabase().execSQL(deleteSql);
        }

        if(removedListener != null){//notify remove listener
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s, %s\n", Column.RAW_KEY, Column.content));
            sb.append(String.format("FROM %s \n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            if (enableExpireTime()) {
                sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
            }
            sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(getOrderPolicy())));
            sb.append(String.format("LIMIT %s, %s\n", maxCount, maxCount + 1000000));
            String query = sb.toString();
            Log.d("FastSetHedisImpl trimCount query disk data sql " + query);
            Cursor cursor = diskDataBaseHelper.getReadableDatabase().rawQuery(query, null);
            if (null == cursor || !cursor.moveToFirst()) {//delete none? some thing bad happens! retry again.
                IOUtil.safeClose(cursor);
                sync();
                return;
            }

            do {
                String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
                byte[] body= cursor.getBlob(cursor.getColumnIndex(Column.content));
                notifyRemoveListener(rawKey, body);
            } while (cursor.moveToNext());
            IOUtil.safeClose(cursor);
        }

        {//remove from disk storage
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s \n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
            if (enableExpireTime()) {
                sb.append(String.format("WHERE %s>%s AND %s\n", Column.expireTime, systemTime, deleteSelection));
            }else{
                sb.append(String.format("WHERE %s\n", deleteSelection));
            }
            String deleteSql = sb.toString();
            Log.d("FastSetHedisImpl trimCount delete disk database run sql " + deleteSql);
            diskDataBaseHelper.getWritableDatabase().execSQL(deleteSql);
        }
    }

    @Override
    public synchronized void trimToSize(int maxSize) {
        if(enableExpireTime()) clearTrash();
        this.maxSize = maxSize;
        if(currentSize <= maxSize){
            Log.d("FastSetHedisImpl trimToSize, Current Size "+currentSize+" less than "+maxSize);
            return ;
        }

        long systemTime = TimeUtils.getCurrentWallClockTime();
        String deleteSelection = "1=1";
        {//get the will deleted entries.
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("SELECT %s, %s, %s, %s\n", Column.RAW_KEY, Column.SIZE, Column.accessTime, Column.expireTime));
            sb.append(String.format("FROM %s \n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
            if (enableExpireTime()) {
                sb.append(String.format("WHERE %s>%s\n", Column.expireTime, systemTime));
            }
            sb.append(String.format("ORDER BY %s\n", getRevertQueryOrderSelection(getOrderPolicy())));
            String query = sb.toString();
            Log.d("FastSetHedisImpl trimToSize query sql " + query);
            Cursor cursor = memoryIndexDataBaseHelper.getReadableDatabase().rawQuery(query, null);
            if (null == cursor || !cursor.moveToLast()) {//it's not work normal, sync to try again
                IOUtil.safeClose(cursor);
                sync();
                return;
            }
            long deltaSize = currentSize - maxSize;
            do {
                String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
                int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
                deltaSize -= size;
                onEntryRemoved(rawKey, size);
            } while (deltaSize > 0 && cursor.moveToPrevious());
            deleteSelection = getDeleteWhereSelection(orderPolicy, cursor);

            {//delete entries from memory database
                StringBuilder sb1 = new StringBuilder();
                sb1.append(String.format("DELETE FROM %s\n", FastLinkedHashMapDataBaseHelper.TABLE_NAME));
                if (enableExpireTime()) {
                    sb1.append(String.format("WHERE %s>%s AND %s\n", Column.expireTime, systemTime, deleteSelection));
                } else {
                    sb1.append(String.format("WHERE %s\n", deleteSelection));
                }
                String sql = sb1.toString();
                Log.d("FastSetHedisImpl trimSize memory delete sql " + sql);
                memoryIndexDataBaseHelper.getWritableDatabase().execSQL(sql);
            }

            if (cursor.getPosition() < 0) {//delete all
                if (removedListener != null) {
                    try {
                        List<ThreeTuple<String, String, T>> res = getAll();
                        notifyRemoveListener(res);
                    } catch (HedisException e) {
                        e.printStackTrace();
                    }
                }
                evictAll();
                StringBuilder dsb = new StringBuilder();
                dsb.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
                dsb.append(String.format("WHERE %s='%s'\n", Column.TAG, parserKey(getTag())));
                String sql = dsb.toString();
                Log.d("FastSetHedisImpl trimToSize delete sql " + sql);
                diskDataBaseHelper.getWritableDatabase().execSQL(sql);
            } else {//delete some entries

                if (null != removedListener) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(String.format("SELECT %s, %s\n", Column.content, Column.RAW_KEY));
                    sb1.append(String.format("FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
                    if (enableExpireTime()) {
                        sb1.append(String.format("WHERE %s='%s' AND %s>%s AND %s\n", Column.TAG,
                                parserKey(getTag()), Column.expireTime, systemTime, deleteSelection));
                    } else {
                        sb1.append(String.format("WHERE %s='%s' AND %s\n", Column.TAG, parserKey(getTag()), deleteSelection));
                    }
                    String sql = sb1.toString();
                    Log.d("FastSetHedisImpl trimSize get all need removed sql " + sql);
                    Cursor resCursor = diskDataBaseHelper.getReadableDatabase().rawQuery(sql, null);
                    if (null == resCursor || !resCursor.moveToFirst()) {//do not delete any entry,ti work failed
                        IOUtil.safeClose(resCursor);
                        sync();
                        return;
                    }
                    do {
                        String rawKey = resCursor.getString(resCursor.getColumnIndex(Column.RAW_KEY));
                        byte[] body = resCursor.getBlob(resCursor.getColumnIndex(Column.content));
                        notifyRemoveListener(rawKey, body);
                    } while (resCursor.moveToNext());
                    IOUtil.safeClose(cursor);
                }

                StringBuilder sb1 = new StringBuilder();
                sb1.append(String.format("DELETE FROM %s\n", LinkedHashMapHedisDataBaseHelper.TABLE_NAME));
                if (enableExpireTime()) {
                    sb1.append(String.format("WHERE %s='%s' AND %s>%s AND %s\n", Column.TAG,
                            parserKey(getTag()), Column.expireTime, systemTime, deleteSelection));
                } else {
                    sb1.append(String.format("WHERE %s='%s' AND %s\n", Column.TAG, parserKey(getTag()), deleteSelection));
                }
                String sql = sb1.toString();
                Log.d("FastSetHedisImpl trimSize real delete sql " + sql);
                diskDataBaseHelper.getWritableDatabase().execSQL(sql);
            }

            IOUtil.safeClose(cursor);
        }
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public boolean enableExpireTime() {
        return supportTimeRestrict;
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
}
