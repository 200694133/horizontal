package hyn.com.datastorage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import hyn.com.datastorage.db.BaseSQLiteOpenHelper.Column;
import hyn.com.lib.IOUtil;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.logging.Log;

/**
 * Created by hanyanan on 2015/4/20.
 */
public class BasicQueueDataStorageImpl implements BasicQueueStructureDataStorage {
    protected static final String TABLE_NAME = BasicDataBaseHelper.QUEUE_TABLE_NAME;
    protected final BasicDataBaseHelper basicDataBaseHelper;
    protected final String tag;
    protected final AtomicBoolean disposed = new AtomicBoolean(false);
    public BasicQueueDataStorageImpl(String tag, BasicDataBaseHelper basicDataBaseHelper) {
        this.basicDataBaseHelper = basicDataBaseHelper;
        this.tag = tag;
    }

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
    public int size() {
        checkState();
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT SUM(%s) AS SIZE\n", BaseSQLiteOpenHelper.Column.SIZE));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", BaseSQLiteOpenHelper.Column.KEY, getEncodedTag(), BaseSQLiteOpenHelper.Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", BaseSQLiteOpenHelper.Column.KEY, getEncodedTag()));
        }
        String querySql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl size query run sql " + querySql);

        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(querySql,null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return 0;
        }
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl getGroupSize cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("SIZE"));

        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl getGroupSize Size " +count);
        IOUtil.safeClose(cursor);

        return count;
    }

    @Override
    public int count() {
        checkState();
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT COUNT(%s) AS COUNT\n", BaseSQLiteOpenHelper.Column.KEY));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", BaseSQLiteOpenHelper.Column.KEY, getEncodedTag(), BaseSQLiteOpenHelper.Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", BaseSQLiteOpenHelper.Column.KEY, getEncodedTag()));
        }
        String querySql = sb.toString();
        Log.d(TABLE_NAME, "QueueDataStorageImpl count run sql " + querySql);

        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(querySql,null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return 0;
        }
        Log.d(TABLE_NAME, "QueueDataStorageImpl count cursor count " +cursor.getCount());
        int count = cursor.getInt(cursor.getColumnIndex("COUNT"));
        Log.d(TABLE_NAME, "QueueDataStorageImpl count count " + count);
        IOUtil.safeClose(cursor);

        return count;
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间的负值作为优先级。确保
     * 优先级数值为最小，从而排在前面
     *
     * @see #pushHead(byte[])
     * @see #pushTail(byte[])
     * @see #pushTail(byte[], Long)
     * @param content the content to push
     * @param expireTime
     */
    public void pushHead(byte[] content, Long expireTime) {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        if(null == content || content.length == 0) {
            Log.e(TABLE_NAME, "pushHead cannot read anything from input stream!");
            return ;
        }
        long ex = expireTime == null ? Long.MAX_VALUE:expireTime;
        doPut(content, -systemTime, ex);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间作为优先级。确保
     * 优先级数值为最大，从而排在后面
     *
     * @see #pushHead(byte[])
     * @see #pushTail(byte[])
     * @see #pushHead(byte[], Long)
     * @param content the content to push
     * @param expireTime
     */
    public void pushTail(byte[] content, Long expireTime) {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        if(null == content || content.length == 0) {
            Log.e(TABLE_NAME, "pushHead cannot read anything from input stream!");
            return ;
        }
        long ex = expireTime == null ? Long.MAX_VALUE:expireTime;
        doPut(content, systemTime, ex);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间的负值作为优先级。确保
     * 优先级数值为最小，从而排在前面
     *
     * @see #pushHead(byte[],Long)
     * @see #pushTail(byte[])
     * @see #pushTail(byte[], Long)
     * @param content the content to push
     */
    public void pushHead(byte[] content) {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        if(null == content || content.length == 0) {
            Log.e(TABLE_NAME, "pushHead cannot read anything from input stream!");
            return ;
        }
        doPut(content, -systemTime, Long.MAX_VALUE);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间作为优先级。确保
     * 优先级数值为最大，从而排在后面
     *
     * @see #pushHead(byte[])
     * @see #pushTail(byte[], Long)
     * @see #pushHead(byte[], Long)
     * @param content the content to push
     */
    public void pushTail(byte[] content) {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        if(null == content || content.length == 0) {
            Log.e(TABLE_NAME, "pushHead cannot read anything from input stream!");
            return ;
        }
        doPut(content, systemTime, Long.MAX_VALUE);
    }

    /**
     * Do the put action to insert one entry to the database.
     * @param content the content will insert to database
     * @param priority the priority
     * @param expireTime the expire time.
     */
    private void doPut(byte[] content, long priority, long expireTime){
        checkState();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BaseSQLiteOpenHelper.Column.KEY, getEncodedTag());
        contentValues.put(BaseSQLiteOpenHelper.Column.RAW_KEY, getTag());
        contentValues.put(BaseSQLiteOpenHelper.Column.EXPIRE_TIME, expireTime);
        contentValues.put(BaseSQLiteOpenHelper.Column.PRIORITY, priority);
        contentValues.put(BaseSQLiteOpenHelper.Column.SIZE, content.length);
        contentValues.put(BaseSQLiteOpenHelper.Column.CONTENT, content);
        getSQLiteHelper().getWritableDatabase().insertWithOnConflict(TABLE_NAME, null,
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**  Return the last entry with update the last access time. */
    public byte[] tail() {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s\n", Column.CONTENT, Column.ID));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s DESC\n", BaseSQLiteOpenHelper.Column.PRIORITY));
        sb.append(String.format("LIMIT 1\n"));
        String querySql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl tail run sql " + querySql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d(TABLE_NAME, "QueueDataStorageImpl tail  cursor count " +cursor.getCount());
        byte[] body = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.CONTENT));
        int id = cursor.getInt(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.ID));
        IOUtil.safeClose(cursor);
        return body;
    }

    @Override
    public byte[] takeTail() {
        byte[] res = tail();
        deleteTail();
        return res;
    }

    /**
     * Return the first entry with update the last access time.
     * @return
     */
    public byte[] head() {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s\n", Column.CONTENT, Column.ID));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s ASC\n", BaseSQLiteOpenHelper.Column.PRIORITY));
        sb.append(String.format("LIMIT 1\n"));
        String querySql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl head run sql " + querySql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d(TABLE_NAME, "QueueDataStorageImpl head cursor count " +cursor.getCount());
        byte[] body = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.CONTENT));
        int id = cursor.getInt(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.ID));
        IOUtil.safeClose(cursor);
        return body;
    }

    @Override
    public byte[] takeHead() {
        byte[] res = head();
        deleteHead();
        return res;
    }

    /**
     * Return all entry without update the last access time attribute.
     * @return
     */
    public List<byte[]> getAll() {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s ASC\n", BaseSQLiteOpenHelper.Column.PRIORITY));

        String querySql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl getAll run sql " + querySql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl getAll cursor count " +cursor.getCount());
        final List<byte[]> res = new ArrayList<>();
        do {
            byte[] body = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.CONTENT));
            res.add(body);
        }while(cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    /**
     * Return request page entry without update the last access time attribute.
     */
    public List<byte[]> getPage(int pageIndex, int count)  {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s ASC\n", BaseSQLiteOpenHelper.Column.PRIORITY));
        sb.append(String.format("LIMIT %s, %s\n", pageIndex*count, pageIndex*count+count));

        String querySql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl getPage run sql " + querySql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return null;
        }
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl getPage cursor count " +cursor.getCount());
        final List<byte[]> res = new ArrayList<>();
        do {
            byte[] body = cursor.getBlob(cursor.getColumnIndex(BaseSQLiteOpenHelper.Column.CONTENT));
            res.add(body);
        }while(cursor.moveToNext());
        IOUtil.safeClose(cursor);
        return res;
    }

    @Override
    public void deleteTail() {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s = (\n", Column.ID));
        sb.append(String.format("SElECT %s \n FROM %s\n", Column.ID, TABLE_NAME));
        if(enableExpireRestriction()) {
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        } else {
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s DESC\n", BaseSQLiteOpenHelper.Column.PRIORITY));
        sb.append(String.format("LIMIT 1 )\n"));

        String sql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl deleteTail run sql " + sql);
        getSQLiteHelper().getWritableDatabase().execSQL(sql);
    }

    @Override
    public void deleteHead() {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s = (\n", Column.ID));
        sb.append(String.format("SElECT %s \n FROM %s\n", Column.ID, TABLE_NAME));
        if(enableExpireRestriction()) {
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        } else {
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s ASC\n", Column.PRIORITY));
        sb.append(String.format("LIMIT 1 )\n"));

        String sql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl deleteHead run sql " + sql);
        getSQLiteHelper().getWritableDatabase().execSQL(sql);
    }

    @Override
    public void trimCount(int maxRowCount) {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s AND %s >= (\n", Column.KEY, getEncodedTag(),
                    Column.EXPIRE_TIME, currTime, Column.PRIORITY));
        }else{
            sb.append(String.format("WHERE %s='%s' AND %s >= (\n", Column.KEY, getEncodedTag(),Column.PRIORITY));
        }
        sb.append(String.format("SElECT %s \n FROM %s\n", Column.PRIORITY, TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s ASC\n", Column.PRIORITY));
        sb.append(String.format("LIMIT 1 OFFSET %s)\n", maxRowCount));

        String sql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl trimCount run sql " + sql);
        getSQLiteHelper().getWritableDatabase().execSQL(sql);
    }

    @Override
    public void trimSize(int maxSize) {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s,%s\n", Column.SIZE, Column.PRIORITY));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, getEncodedTag(), Column.EXPIRE_TIME, currTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, getEncodedTag()));
        }
        sb.append(String.format("ORDER BY %s ASC\n", Column.PRIORITY));
        String sql = sb.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl trimSize get all size run sql " + sql);
        Cursor cursor = getSQLiteHelper().getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return ;
        }

        int currSize = 0;
        do{
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            currSize += size;
        }while(currSize < maxSize && cursor.moveToNext());
        Log.d(TABLE_NAME, "Current size "+currSize+"\tmaxSize "+maxSize);

        if(currSize < maxSize) {
            return ;
        }
        long priority = cursor.getLong(cursor.getColumnIndex(Column.PRIORITY));
        IOUtil.safeClose(cursor);

        if(priority == 0) return ;
        StringBuilder delete = new StringBuilder();
        delete.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            delete.append(String.format("WHERE %s='%s' AND %s>%s AND %s >= %s\n", Column.KEY, getEncodedTag(),
                    Column.EXPIRE_TIME, currTime, Column.PRIORITY, priority));
        }else{
            delete.append(String.format("WHERE %s='%s' AND %s >= %s\n", Column.KEY, getEncodedTag(),Column.PRIORITY,priority));
        }
        String deleteSql = delete.toString();
        Log.d(TABLE_NAME, "BasicQueueDataStorageImpl trimSize run delete sql " + deleteSql);
        getSQLiteHelper().getWritableDatabase().execSQL(deleteSql);
    }

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public void clearTrash() {
        if(enableExpireRestriction()){
            long curr = TimeUtils.getCurrentWallClockTime();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
            sb.append(String.format("WHERE %s='%s' AND %s <= %s\n", Column.KEY, getEncodedTag(),Column.EXPIRE_TIME, curr));
            String deleteSql = sb.toString();
            Log.d(TABLE_NAME, "BasicQueueDataStorageImpl clearTrash run delete sql " + deleteSql);
            getSQLiteHelper().getWritableDatabase().execSQL(deleteSql);
        }
    }

    @Override
    public boolean isExpired(String key) {
        return false;
    }

    @Override
    public boolean enableExpireRestriction() {
        return false;
    }
}
