package hyn.com.datastorage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import hyn.com.datastorage.db.BaseSQLiteOpenHelper.Column;
import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.android.parser.ObjectParser;
import hyn.com.lib.IOUtil;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.logging.Log;
import hyn.com.lib.binaryresource.BinaryResource;
import hyn.com.lib.binaryresource.ByteArrayBinaryResource;

/**
 * Created by hanyanan on 2015/4/23.
 */
public class OrderDataStorageImpl<T> implements OrderStructureDataStorage<T> {
    protected static final String TABLE_NAME = BasicDataBaseHelper.ORDER_TABLE_NAME;
    protected final BasicDataBaseHelper basicDataBaseHelper;
    protected final String tag;
    protected final AtomicBoolean disposed = new AtomicBoolean(false);
    protected final OrderPolicy orderPolicy;
    protected final ObjectParser<T> objectParser;
    protected final OrderPropertyAttacher orderPropertyAttacher;
    public OrderDataStorageImpl(String tag, ObjectParser<T> parser, OrderPolicy orderPolicy,
                                                OrderPropertyAttacher orderPropertyAttacher,
                                                BasicDataBaseHelper basicDataBaseHelper){
        this.basicDataBaseHelper = basicDataBaseHelper;
        this.tag = tag;
        this.orderPolicy = orderPolicy;
        this.objectParser = parser;
        this.orderPropertyAttacher = orderPropertyAttacher;
    }

    /**
     *
     */

    /**
     * Check if current is correct. It may be throw IllegalStateException when call in disposed state.
     */
    protected void checkState() {
        if(disposed.get()){
            throw new IllegalStateException("");
        }
    }

    @Override
    public String getTag() {
        return tag;
    }

    protected BaseSQLiteOpenHelper getSQLiteHelper(){
        return basicDataBaseHelper;
    }

    /**
     * Return the check code of md5 as the key.
     * @return
     */
    protected String getEncodedTag(){
        return ValueUtil.md5_16(getTag());
    }

    @Override
    public OrderPolicy getOrderPolicy() {
        return orderPolicy;
    }

    final String parserKey(String key){ return ValueUtil.md5_16(key); }

    @Override
    public ObjectParser<T> getParser() {
        return objectParser;
    }

    @Override
    public boolean exist(String key) {
        return orderPropertyAttacher.exist(key);
    }

    /**
     * Retuen the tuple of key-value
     * @return
     * @throws ParseFailedException
     */
    public TwoTuple<String, T> eldest() throws ParseFailedException {
        String key = orderPropertyAttacher.eldest(getOrderPolicy());
        if(ValueUtil.isEmpty(key)) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s'\n", Column.KEY, parserKey(key)));
        String sql = sb.toString();
        Log.d(TABLE_NAME,"OrderDataStorageImpl run sql " + sql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.CONTENT));
        IOUtil.safeClose(cursor);
        return new TwoTuple(key, getParser().transferToObject(body));
    }

    @Override
    public void put(String key, T content) throws ParseFailedException {
        byte[] body = getParser().transferToBlob(content);
        orderPropertyAttacher.put(key, body.length, Long.MAX_VALUE);
        doPut(key, body, Long.MAX_VALUE);
    }

    @Override
    public void put(String key, InputStream inputStream) throws ParseFailedException {
        byte[] body = IOUtil.inputStreamToBytes(inputStream);
        orderPropertyAttacher.put(key, body.length, Long.MAX_VALUE);
        doPut(key, body, Long.MAX_VALUE);
    }

    @Override
    public void put(String key, T content, Long expireTime) throws ParseFailedException {
        byte[] body = getParser().transferToBlob(content);
        expireTime = expireTime==null?Long.MAX_VALUE:expireTime;
        orderPropertyAttacher.put(key, body.length, expireTime);
        doPut(key, body, expireTime);
    }

    @Override
    public void put(String key, InputStream inputStream, Long expireTime) throws ParseFailedException {
        byte[] body = IOUtil.inputStreamToBytes(inputStream);
        expireTime = expireTime==null?Long.MAX_VALUE:expireTime;
        orderPropertyAttacher.put(key, body.length, expireTime);
        doPut(key, body, expireTime);
    }

    private void doPut(String key, byte[] content, Long expireTime){
        long systemTime = TimeUtils.getCurrentWallClockTime();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.KEY, parserKey(key));
        contentValues.put(Column.RAW_KEY, key);
        contentValues.put(Column.EXPIRE_TIME, expireTime == null ? Long.MAX_VALUE:expireTime);
        contentValues.put(Column.SIZE, content.length);
        contentValues.put(Column.LAST_ACCESS_TIME, systemTime);
        contentValues.put(Column.TAG, parserKey(getTag()));
        contentValues.put(Column.RAW_TAG, getTag());
        contentValues.put(Column.MODIFY_TIME, systemTime);
        contentValues.put(Column.CONTENT, content);
        contentValues.put(Column.CREATE_TIME, systemTime);
        getSQLiteHelper().getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);

        trimToCount(orderPropertyAttacher.maxCount());
        trimToSize(orderPropertyAttacher.maxSize());
    }

    @Override
    public List<TwoTuple<String, T>> getAll() throws ParseFailedException {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.TAG, getEncodedTag(), Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.TAG, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s\n", FastOrderPropertyAttacher.getQueryOrderSelection(orderPolicy)));

        String sql = sb.toString();
        Log.d(TABLE_NAME,"OrderDataStorageImpl getAll run sql " + sql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d(TABLE_NAME,"OrderDataStorageImpl getAll cursor count " + cursor.getCount());
        final List<TwoTuple<String, T>> res = new ArrayList<>();
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.CONTENT));
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            TwoTuple<String, T> element = new TwoTuple<>(rawKey,getParser().transferToObject(body));
            res.add(element);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    @Override
    public List<TwoTuple<String, T>> getPage(int pageIndex, int pageCount) throws ParseFailedException {
        String where = orderPropertyAttacher.getPage(pageIndex, pageCount, getOrderPolicy());
        if(ValueUtil.isEmpty(where)) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.RAW_KEY, Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s\n", Column.TAG, getEncodedTag(), where));//where已经包含时间限制条件，不需要在此判断
        sb.append(String.format("ORDER BY %s\n", FastOrderPropertyAttacher.getQueryOrderSelection(orderPolicy)));

        String sql = sb.toString();
        Log.d(TABLE_NAME,"OrderDataStorageImpl getAll run sql " + sql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d(TABLE_NAME,"OrderDataStorageImpl getAll cursor count " + cursor.getCount());
        final List<TwoTuple<String, T>> res = new ArrayList<>();
        do{
            byte[] body = cursor.getBlob(cursor.getColumnIndex(Column.CONTENT));
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            TwoTuple<String, T> element = new TwoTuple<>(rawKey,getParser().transferToObject(body));
            res.add(element);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    @Override
    public TwoTuple<String, T> get(String key) throws ParseFailedException {
        if(!orderPropertyAttacher.get(key)){
            return null;
        }
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s='%s'\n AND %s>%s", Column.TAG, getEncodedTag(),
                    Column.KEY, parserKey(key), Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s' AND %s='%s'\n", Column.TAG, getEncodedTag(), Column.KEY, parserKey(key)));
        }
        String sql = sb.toString();
        Log.d(TABLE_NAME,"OrderDataStorageImpl get run sql "+sql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(sql,  null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return null;
        }
        byte []body = cursor.getBlob(cursor.getColumnIndex(Column.CONTENT));
        IOUtil.safeClose(cursor);
        return new TwoTuple<>(key, getParser().transferToObject(body));
    }

    @Override
    public TwoTuple<String, BinaryResource> getInputStream(String key) {
        if(!orderPropertyAttacher.get(key)){
            return null;
        }
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s='%s'\n AND %s>%s", Column.TAG, getEncodedTag(),
                    Column.KEY, parserKey(key), Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s' AND %s='%s'\n", Column.TAG, getEncodedTag(), Column.KEY, parserKey(key)));
        }
        String sql = sb.toString();
        Log.d(TABLE_NAME,"OrderDataStorageImpl get run sql "+sql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(sql,  null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return null;
        }
        byte []body = cursor.getBlob(cursor.getColumnIndex(Column.CONTENT));
        IOUtil.safeClose(cursor);
        return new TwoTuple<String, BinaryResource>(key, new ByteArrayBinaryResource(body));
    }

    @Override
    public void remove(String key) {
        if(orderPropertyAttacher.exist(key)){
            orderPropertyAttacher.remove(key);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, parserKey(key)));
            String sql = sb.toString();
            Log.d(TABLE_NAME,"OrderDataStorageImpl remove run sql " + sql);
            getSQLiteHelper().getWritableDatabase().execSQL(sql);
        }
    }

    @Override
    public TwoTuple<String, T> fetch(String key) throws ParseFailedException {
        TwoTuple<String, T> res = get(key);
        if(null == res) return null;
        remove(key);
        return res;
    }

    @Override
    public TwoTuple<String, BinaryResource> fetchStream(String key) {
        TwoTuple<String, BinaryResource> res = getInputStream(key);
        if(null == res) return null;
        remove(key);
        return res;
    }

    @Override
    public void clear() {
        orderPropertyAttacher.clear();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        String sql = sb.toString();
        Log.d(TABLE_NAME,"OrderDataStorageImpl clear run sql " + sql);
        getSQLiteHelper().getWritableDatabase().execSQL(sql);
    }

    @Override
    public int size() {
        clearTrash();
        return orderPropertyAttacher.size();
    }

    @Override
    public int maxSize() {
        return 0;
    }

    @Override
    public int count() {
        clearTrash();
        return orderPropertyAttacher.count();
    }

    @Override
    public int maxCount() {
        return 0;
    }

    @Override
    public void trimToCount(int maxRowCount) {
        clearTrash();
        String where = orderPropertyAttacher.trimToCount(maxRowCount, getOrderPolicy());//已经包含时间限制
        if(ValueUtil.isEmpty(where)) return ;
        String sql = "DELETE FROM " + TABLE_NAME + "\n WHERE " + where;
        Log.d(TABLE_NAME,"OrderDataStorageImpl trimToCount run sql " + sql);
        getSQLiteHelper().getWritableDatabase().execSQL(sql);
    }

    @Override
    public void trimToSize(int maxSize) {
        clearTrash();
        String where = orderPropertyAttacher.trimToSize(maxSize, getOrderPolicy());//已经包含时间限制
        if(ValueUtil.isEmpty(where)) return ;
        String sql = "DELETE FROM " + TABLE_NAME + "\n WHERE " + where;
        Log.d(TABLE_NAME,"OrderDataStorageImpl trimToSize run sql " + sql);
        getSQLiteHelper().getWritableDatabase().execSQL(sql);
    }

    @Override
    public void setRemoveListener(OnOverFlowListener listener) {
        orderPropertyAttacher.setRemoveListener(listener);
    }

    @Override
    public void clearTrash() {
        if(!orderPropertyAttacher.clearTrash()) return ;
        StringBuilder deleteSB = new StringBuilder();
        deleteSB.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        deleteSB.append(String.format("WHERE %s<=%s\n", Column.EXPIRE_TIME, TimeUtils.getCurrentWallClockTime()));
        String sql = deleteSB.toString();
        Log.d(TABLE_NAME,"OrderDataStorageImpl clearTrash from memory database run sql " + sql);
        getSQLiteHelper().getWritableDatabase().execSQL(sql);
    }

    @Override
    public boolean isExpired(String key) {
        return orderPropertyAttacher.isExpired(key);
    }

    @Override
    public boolean enableExpireRestriction() {
        return orderPropertyAttacher.enableExpireRestriction();
    }
}
