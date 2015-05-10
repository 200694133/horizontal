package hyn.com.datastorage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

import hyn.com.datastorage.db.OrderStructureDataStorage.OrderPolicy;
import hyn.com.lib.IOUtil;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.logging.Log;

/**
 * Created by hanyanan on 2015/4/23.
 * 使用内存数据库加快查询速度
 */
public class FastOrderPropertyAttacher extends BaseSQLiteOpenHelper implements OrderPropertyAttacher {
    public static final int VERSION = 1;
    public static final String TABLE_NAME = "Fast_Order";
    public static final String CREATE_TABLE_SQL = String.format("CREATE TABLE %s (\n" +
                    "%s CHAR(16) NOT NULL PRIMARY KEY,\n" + //key
                    "%s INTEGER,\n" + //size
                    "%s LONG,\n" + //access time
                    "%s LONG,\n" + //expire time
                    "%s TEXT)\n", //raw key
            TABLE_NAME,
            Column.KEY,Column.SIZE, Column.LAST_ACCESS_TIME, Column.EXPIRE_TIME, Column.RAW_KEY);
    //current all size, include both expired time entry and invalid entries
    private int currentSize;
    //current all size, include both expired time entry and invalid entries
    private int currentCount;
    //the max size of the current tag, if it's bigger than current value, than delete from both
    // expired entries and the tail of the current.
    protected int maxSize = Integer.MAX_VALUE;
    //the max size of the current tag, if it's bigger than current value, than delete from both
    // expired entries and the tail of the current.
    protected int maxCount = Integer.MAX_VALUE;

    //The remove listener.
    protected OnOverFlowListener removedListener;

    private final String tag;
    public FastOrderPropertyAttacher(Context context, String tag) {
        super(context, null, null, VERSION);
        this.tag = tag;
    }

    public final String getTag(){
        return tag;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    protected String getEncodedTag(){
        return ValueUtil.md5_16(tag);
    }

    protected String encode(String s){
        return ValueUtil.md5_16(getTag());
    }

    final String parserKey(String key){ return ValueUtil.md5_16(key); }

    protected synchronized void onEntryAdded(String key, int size){
        currentSize += size;
        currentCount ++;
        Log.d(TABLE_NAME, "FastSetHedisImpl onEntryAdded " + key + "\tnew size " + currentSize + "\tnew count " + currentCount);
    }

    protected synchronized void onEntryRemoved(String key, int size){
        currentSize -= size;
        currentCount --;
        Log.d(TABLE_NAME,"FastSetHedisImpl onEntryRemoved "+key+"\tnew size "+currentSize+"\tnew count "+currentCount);
    }

    protected synchronized void onEntryChanged(String key, int oldSize, int newSize){
        currentSize = currentSize - oldSize + newSize;
        Log.d(TABLE_NAME,"FastSetHedisImpl onEntryChanged "+key+"\tnew size "+key+" oldSize "+oldSize+" , newSize "+newSize);
    }

    protected synchronized void onClear(){
        currentCount = currentSize = 0;
        Log.d(TABLE_NAME,"FastSetHedisImpl onClear!");
    }

    @Override
    public synchronized void sync(BasicDataBaseHelper helper) {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s, %s, %s\n", Column.RAW_KEY, Column.SIZE, Column.LAST_ACCESS_TIME, Column.EXPIRE_TIME));
        sb.append(String.format("FROM %s\n", BasicDataBaseHelper.ORDER_TABLE_NAME));
        if(enableExpireRestriction()) {
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n",Column.TAG, getEncodedTag(), Column.EXPIRE_TIME, systemTime));
        }
        String sql = sb.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher sync run sql " + sql);
        Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return ;
        }

        do{
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            long lastAccessTime = cursor.getLong(cursor.getColumnIndex(Column.LAST_ACCESS_TIME));
            long expireTime = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
            String key = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            put(key, size, lastAccessTime, expireTime);
        }while (cursor.moveToNext());

        IOUtil.safeClose(cursor);
    }

    /**
     * 仅仅是同步当前数据库的条数和大小，不与磁盘数据库同步
     * @see #sync(BasicDataBaseHelper)
     */
    private synchronized void reSync(){
        currentCount = currentSize = 0;
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT SUM(%s) AS Size, Count(%s) AS Count\n", Column.SIZE, Column.SIZE));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()) {
            sb.append(String.format("WHERE %s>%s\n", Column.EXPIRE_TIME, systemTime));
        }
        String sql = sb.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher reSync run sql "+sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);

        if(cursor == null || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return ;
        }
        currentSize = cursor.getInt(cursor.getColumnIndex("Size"));
        currentCount = cursor.getInt(cursor.getColumnIndex("Count"));
        IOUtil.safeClose(cursor);
    }

    /**
     * Update last access time for the specify entry.
     * @param rawKey the specify key
     * */
    private void updateAccessTime(final String rawKey){
        long curr = TimeUtils.getCurrentWallClockTime();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.KEY, parserKey(rawKey));
        contentValues.put(Column.LAST_ACCESS_TIME, curr);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Update last access time for the specify entry.
     * @param rawKey the specify key
     * */
    private void updateAccessTime(final String rawKey, long accessTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.KEY, parserKey(rawKey));
        contentValues.put(Column.LAST_ACCESS_TIME, accessTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }


    /** 返回优先级最低的key,并且会更新最后访问时间. */
    public String eldest(OrderPolicy orderPolicy) {
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.RAW_KEY));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()) {
            sb.append(String.format("WHERE %s>%s\n", Column.EXPIRE_TIME, systemTime));
        }
        sb.append(String.format("ORDER BY %s\n", getRevertQueryOrderSelection(orderPolicy)));
        sb.append(String.format("LIMIT 1\n"));
        String sql = sb.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher to get eldest to run sql "+sql);
        //query the eldest entry.
        Cursor cursor = getReadableDatabase().rawQuery(sql ,null);
        if(null == cursor || !cursor.moveToFirst()){//no entry currently. something error happened..
            IOUtil.safeClose(cursor);
            return null;
        }
        String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
        IOUtil.safeClose(cursor);
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher eldest key "+rawKey);
        return rawKey;
    }

    private int getSize(String rawKey){
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.SIZE));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()) {
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n", Column.KEY, parserKey(rawKey), Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, parserKey(rawKey)));
        }
        String sql = sb.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher getSize to run sql "+sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql ,null);
        if(null == cursor || !cursor.moveToFirst()){//no entry currently. something error happened..
            IOUtil.safeClose(cursor);
            return -1;
        }
        int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
        IOUtil.safeClose(cursor);
        return size;
    }

    @Override
    public void put(String rawKey, int size, long accessTime, long expireTime) {
        int preSize = getSize(rawKey);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.KEY, parserKey(rawKey));
        contentValues.put(Column.RAW_KEY, rawKey);
        contentValues.put(Column.EXPIRE_TIME, expireTime<=0?Long.MAX_VALUE:expireTime);
        contentValues.put(Column.SIZE, size);
        contentValues.put(Column.LAST_ACCESS_TIME, accessTime<=0?TimeUtils.getCurrentWallClockTime():accessTime);
        getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        if(preSize <= 0){
            onEntryAdded(rawKey, size);
        }else{
            onEntryChanged(rawKey, preSize, size);
        }
    }

    @Override
    public void put(String rawKey, int size, long expireTime) {
        put(rawKey, size,-1 ,expireTime);
    }

    /**
     * 返回当前page的条件语句,注意返回的查询条件已经包含了时间限制。
     * @param pageIndex
     * @param pageCount
     * @param orderPolicy
     * @return
     */
    public String getPage(int pageIndex, int pageCount, OrderPolicy orderPolicy) {
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder whereSB = new StringBuilder();
        whereSB.append(String.format("SELECT %s, %s, %s\n", Column.LAST_ACCESS_TIME, Column.SIZE, Column.EXPIRE_TIME));
        whereSB.append(String.format("FROM %s\n", TABLE_NAME));
        whereSB.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        whereSB.append(String.format("LIMIT %s, %s\n", pageIndex * pageCount, pageIndex * pageCount + pageCount));
        String where =  whereSB.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher getPage query threshold sql "+where);
        Cursor cursor = getReadableDatabase().rawQuery(where, null);
        if(null == cursor || !cursor.moveToFirst() || !cursor.moveToLast()){//没有选中的page
            IOUtil.safeClose(cursor);
            return null;
        }

        StringBuilder selection = new StringBuilder();
        switch (orderPolicy){
            case LRU: {//按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾,最后访问拥有最高的优先级
                cursor.moveToFirst();
                long lastAccessTime = cursor.getLong(cursor.getColumnIndex(Column.LAST_ACCESS_TIME));//访问时间的上限,最近的时间
                cursor.moveToLast();
                long furthestAccessTime = cursor.getLong(cursor.getColumnIndex(Column.LAST_ACCESS_TIME));//访问时间的下限，最早的时间
                if (lastAccessTime <= 0 || furthestAccessTime <= 0) break;
                selection.append(Column.LAST_ACCESS_TIME + "<=" + lastAccessTime + " AND " + Column.LAST_ACCESS_TIME + " >= " + furthestAccessTime);
                break;
            }
            case TTL: {//按照超时时间排序，剩余有效期越长，优先级越高
                cursor.moveToFirst();
                long longestTTL = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
                cursor.moveToLast();
                long shortestTTL = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
                if (longestTTL <= 0 || shortestTTL <= 0) break;
                selection.append(Column.EXPIRE_TIME + ">=" + shortestTTL + " AND " + Column.EXPIRE_TIME + " <= " + longestTTL);
                break;
            }
            case REVERT_TTL: {//按照超时时间排序，剩余有效期越短，优先级越高，与TTL相反
                cursor.moveToFirst();
                long shortestTTL = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
                cursor.moveToLast();
                long longestTTL = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
                if (longestTTL <= 0 || shortestTTL <= 0) break;
                selection.append(Column.EXPIRE_TIME + ">=" + shortestTTL + " AND " + Column.EXPIRE_TIME + " <= " + longestTTL);
                break;
            }
            case Size: {//按照从小到大，文件越小优先级越高
                cursor.moveToFirst();
                long minSize = cursor.getLong(cursor.getColumnIndex(Column.SIZE));
                cursor.moveToLast();
                long maxSize = cursor.getLong(cursor.getColumnIndex(Column.SIZE));
                if (minSize <= 0 || maxSize <= 0) break;
                selection.append(Column.SIZE + ">=" + minSize + " AND " + Column.SIZE + " <= " + maxSize);
                break;
            }
            case REVERT_SIZE: {//按照从大到小，文件越大优先级越高
                cursor.moveToFirst();
                long maxSize = cursor.getLong(cursor.getColumnIndex(Column.SIZE));
                cursor.moveToLast();
                long minSize = cursor.getLong(cursor.getColumnIndex(Column.SIZE));
                if (minSize <= 0 || maxSize <= 0) break;
                selection.append(Column.SIZE + ">=" + minSize + " AND " + Column.SIZE + " <= " + maxSize);
                break;
            }
        }
        IOUtil.safeClose(cursor);

        if (enableExpireRestriction()) {
            selection.append(String.format(" AND %s>%s\n", Column.EXPIRE_TIME, currTime, where));
        }


        StringBuilder update = new StringBuilder();
        update.append(String.format("UPDATE %s\n", TABLE_NAME));
        update.append(String.format("SET %s=%s\n", Column.LAST_ACCESS_TIME, currTime));
        update.append(String.format("WHERE %s\n", selection));
        String updateSql = update.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher getPage update page run sql "+updateSql);
        getWritableDatabase().execSQL(updateSql);

        return selection.toString();
    }

    /**
     * Check if exist current request entry, do not update the last access time.
     * @param key the specify entry.
     * @return
     */
    public boolean exist(String key) {
        int size = getSize(key);
        return size > 0;
    }

    /**
     * Check if exist current request entry, different from {@link #exist} , this method will update
     * the last access time.
     * @param key the specify entry.
     */
    public boolean get(String key) {
        int size = getSize(key);
        if(size > 0){
            updateAccessTime(key);
        }
        return size > 0;
    }

    @Override
    public void remove(String key) {
        int size = getSize(key);
        if(size > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
            sb.append(String.format("WHERE %s='%s'\n", Column.KEY, parserKey(key)));
            String sql = sb.toString();
            Log.d(TABLE_NAME,"FastOrderPropertyAttacher remove memory index run sql " + sql);
            getWritableDatabase().execSQL(sql);
            onEntryRemoved(key, size);
        }
    }

    @Override
    public void clear() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        String sql = sb.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher clear run sql " + sql);
        getWritableDatabase().execSQL(sql);
        onClear();
    }

    @Override
    public synchronized final int size() {
        return currentSize;
    }

    @Override
    public synchronized final int count() {
        return currentCount;
    }

    @Override
    public synchronized int maxSize() {
        return maxSize;
    }

    @Override
    public synchronized int maxCount() {
        return maxCount;
    }

    /**
     * Return the delete selector..返回删除的条件语句,包含时间限制。
     * @param maxRowCount
     * @return
     */
    public synchronized String trimToCount(int maxRowCount, OrderPolicy orderPolicy) {
        maxCount = maxRowCount;
        if(currentCount <= maxCount){
            Log.d(TABLE_NAME,"FastOrderPropertyAttacher trimToCount, Current count "+currentCount+" less than "+maxCount);
            return null;
        }

        String where;
        long currTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s, %s, %s\n", Column.RAW_KEY, Column.LAST_ACCESS_TIME, Column.SIZE, Column.EXPIRE_TIME));
        sb.append(String.format("FROM %s \n", TABLE_NAME));
        if (enableExpireRestriction()) {
            sb.append(String.format("WHERE %s>%s\n", Column.EXPIRE_TIME, currTime));
        }
        sb.append(String.format("ORDER BY %s\n", getQueryOrderSelection(orderPolicy)));
        sb.append(String.format("LIMIT %s, %s\n", maxCount, maxCount+1000000));
        String query = sb.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher trimCount query sql " + query);
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            Log.e(TABLE_NAME,"FastOrderPropertyAttacher trimToCount can not get any entry, reSync again.");
            reSync();
            return trimToCount(maxRowCount, orderPolicy);
        }
        where = getOutSideSelection(orderPolicy, cursor);
        if(ValueUtil.isEmpty(where)) {
            IOUtil.safeClose(cursor);
            Log.e(TABLE_NAME,"FastOrderPropertyAttacher trimCount can not get any entry, reSync again. ");
            reSync();
            return trimToCount(maxRowCount, orderPolicy);
        }
        final List<String> items = new ArrayList<>();
        cursor.moveToFirst();
        do{
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            String rawKey = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            onEntryRemoved(rawKey, size);
            items.add(rawKey);
        }while (cursor.moveToNext());
        IOUtil.safeClose(cursor);

        notifyRemoveListener(items);
        if(enableExpireRestriction()){
            where = String.format(" %s>%s AND %s\n", Column.EXPIRE_TIME, currTime, where);
        }
        StringBuilder deleteSB = new StringBuilder();
        deleteSB.append(String.format("DELETE FROM %s \n", TABLE_NAME));
        deleteSB.append(String.format("WHERE %s\n", where));
        String deleteSql = deleteSB.toString();
        Log.e(TABLE_NAME,"FastOrderPropertyAttacher trimCount run delete sql " + deleteSql);
        getWritableDatabase().execSQL(deleteSql);

        return where;
    }

    /**
     * Return the delete selector..返回删除的条件语句, 包含时间限制
     * 与{@link #trimToCount(int, OrderStructureDataStorage.OrderPolicy)}不同，
     * 这个每次会尝试只读后面的100条并尝试删除，如果还是不足以达到需要的大小，则进入下一个递归。
     * @param maxSize
     * @return
     */
    public synchronized String trimToSize(int maxSize, OrderPolicy orderPolicy) {
        this.maxSize = maxSize;
        if(currentSize <= maxSize){
            Log.d(TABLE_NAME,"FastOrderPropertyAttacher trimToSize, Current Size "+currentSize+" less than "+maxSize);
            return null;
        }
        long currTime = TimeUtils.getCurrentWallClockTime();
        String where = null;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s, %s, %s\n", Column.RAW_KEY, Column.LAST_ACCESS_TIME, Column.SIZE, Column.EXPIRE_TIME));
        sb.append(String.format("FROM %s \n", TABLE_NAME));
        if (enableExpireRestriction()) {
            sb.append(String.format("WHERE %s>%s\n", Column.EXPIRE_TIME, currTime));
        }
        sb.append(String.format("ORDER BY %s\n", getRevertQueryOrderSelection(orderPolicy)));
        sb.append(String.format("LIMIT 100"));
        String query = sb.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher trimCount query sql " + query);
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            Log.e(TABLE_NAME,"FastOrderPropertyAttacher trimToSize can not get any entry, reSync again.");
            reSync();
            return trimToSize(maxSize, orderPolicy);
        }

        do{
            int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
            String key = cursor.getString(cursor.getColumnIndex(Column.RAW_KEY));
            onEntryRemoved(key, size);
            notifyRemoveListener(key);
        }while(size() > maxSize && cursor.moveToNext());

        if(cursor.isAfterLast()){
            cursor.moveToLast();
        }
        where = getOutSideSelection(orderPolicy, cursor);
        IOUtil.safeClose(cursor);
        if (enableExpireRestriction()) {
            where = String.format("WHERE %s>%s AND %s\n", Column.EXPIRE_TIME, currTime, where);
        }

        //do remove action
        StringBuilder deleteSB = new StringBuilder();
        deleteSB.append(String.format("DELETE FROM %s \n", TABLE_NAME));
        deleteSB.append(String.format("WHERE %s\n", where));
        String deleteSql = deleteSB.toString();
        Log.d(TABLE_NAME,"FastOrderPropertyAttacher trimToSize run delete sql " + deleteSql);
        getWritableDatabase().execSQL(deleteSql);

        Log.d(TABLE_NAME,"FastOrderPropertyAttacher trimToSize current size " + size());
        if(size() > maxSize){ //没有删除干净，继续下一个递归周期
            return trimToSize(maxSize, orderPolicy);
        }

        return where;
    }

    private void notifyRemoveListener(List<String> keyList){
        if(null != removedListener && null != keyList){
            for(String s : keyList){
                removedListener.onOverFlow(s);
            }
        }
    }

    private void notifyRemoveListener(String key){
        if(null != removedListener && null != key){
            removedListener.onOverFlow(key);
        }
    }

    private OnOverFlowListener getOnOverFlowListener(){
        return removedListener;
    }

    @Override
    public void setRemoveListener(OnOverFlowListener listener) {
        this.removedListener = listener;
    }

    /** {@code true} 需要清除过时数据，否则不需要清除 */
    public synchronized boolean clearTrash() {
        if(!enableExpireRestriction()) return false;
        long systemTime = TimeUtils.getCurrentWallClockTime();

//        if not exists（select * from sys.databases where name = 'database_name'）
        StringBuilder querySB = new StringBuilder();
        querySB.append(String.format("SELECT Count(%s) AS Count\n", Column.EXPIRE_TIME));
        querySB.append(String.format("FROM %s\n", TABLE_NAME));
        querySB.append(String.format("WHERE %s<=%s\n", Column.EXPIRE_TIME, systemTime));
        String querySql = querySB.toString();
        Log.d(TABLE_NAME,"FastSetHedisImpl clearTrash query memory database run sql " + querySql);
        Cursor cursor = getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()){
            IOUtil.safeClose(cursor);
            return false;
        }
        int count = cursor.getInt(cursor.getColumnIndex("Count"));
        IOUtil.safeClose(cursor);
        if(count<=0) return false;

        {//delete the expired entry from memory database
            StringBuilder deleteSB = new StringBuilder();
            deleteSB.append(String.format("DELETE FROM %s\n", TABLE_NAME));
            deleteSB.append(String.format("WHERE %s<=%s\n", Column.EXPIRE_TIME, systemTime));
            String sql = deleteSB.toString();
            Log.d(TABLE_NAME,"FastSetHedisImpl clearTrash from memory database run sql " + sql);
            getWritableDatabase().execSQL(sql);
        }

        reSync();

        return true;
    }

    @Override
    public boolean isExpired(String key) {
        StringBuilder querySB = new StringBuilder();
        querySB.append(String.format("SELECT %s\n", Column.EXPIRE_TIME));
        querySB.append(String.format("FROM %s\n", TABLE_NAME));
        querySB.append(String.format("WHERE %s='%s'\n", Column.KEY, parserKey(key)));
        String sql = querySB.toString();
        Log.d(TABLE_NAME,"FastSetHedisImpl isExpired run query sql " + sql);
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return false;
        }
        long time = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
        IOUtil.safeClose(cursor);
        return time<=TimeUtils.getCurrentWallClockTime();
    }

    @Override
    public boolean enableExpireRestriction() {
        return true;
    }


    /**
     * 按照制定的策略给出排序的sql语句
     * @param orderPolicy 排序顺序
     */
    public static String getQueryOrderSelection(OrderPolicy orderPolicy){
        switch (orderPolicy) {
            case LRU://按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾,最后访问拥有最高的优先级
                return Column.LAST_ACCESS_TIME + " DESC ";
            case TTL://按照超时时间排序，剩余有效期越长，优先级越高
                return Column.EXPIRE_TIME + " DESC ";
            case REVERT_TTL://按照超时时间排序，剩余有效期越短，优先级越高，与TTL相反
                return Column.EXPIRE_TIME + " ASC ";
            case Size://按照从小到大，文件越小优先级越高
                return Column.SIZE + " ASC ";
            case REVERT_SIZE://按照从大到小，文件越大优先级越高
                return Column.SIZE + " DESC ";
        }
        throw new IllegalArgumentException("Cannot input default order mode!");
    }

    /**
     * 按照制定的策略给出排序的sql语句,排序的顺序与制定的相反
     * @param orderPolicy 排序顺序
     */
    public static String getRevertQueryOrderSelection(OrderPolicy orderPolicy){
        switch (orderPolicy) {
            case LRU://按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾,最后访问拥有最高的优先级
                return Column.LAST_ACCESS_TIME + " ASC ";
            case TTL://按照超时时间排序，剩余有效期越长，优先级越高
                return Column.EXPIRE_TIME + " ASC ";
            case REVERT_TTL://按照超时时间排序，剩余有效期越短，优先级越高，与TTL相反
                return Column.EXPIRE_TIME + " DESC ";
            case Size://按照从小到大，文件越小优先级越高
                return Column.SIZE + " DESC ";
            case REVERT_SIZE://按照从大到小，文件越大优先级越高
                return Column.SIZE + " ASC ";
        }
        throw new IllegalArgumentException("Cannot input default order mode!");
    }

    public static String getOutSideSelection(OrderPolicy orderPolicy, Cursor cursor){
        String where = null;
        switch (orderPolicy){
            case LRU://按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾,最后访问拥有最高的优先级
                long accessTime = cursor.getLong(cursor.getColumnIndex(Column.LAST_ACCESS_TIME));
                if(accessTime <= 0) break;
                where = Column.LAST_ACCESS_TIME+"<="+accessTime;
                break;
            case TTL://按照超时时间排序，剩余有效期越长，优先级越高
                long ttl = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
                if(ttl <= 0) break;
                where = Column.EXPIRE_TIME+"<="+ttl;
                break;
            case REVERT_TTL://按照超时时间排序，剩余有效期越短，优先级越高，与TTL相反
                long rTTL = cursor.getLong(cursor.getColumnIndex(Column.EXPIRE_TIME));
                if(rTTL <= 0) break;
                where = Column.EXPIRE_TIME+">=" + rTTL;
                break;
            case Size://按照从小到大，文件越小优先级越高
                int size = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
                if(size <= 0) break;
                where = Column.SIZE+">="+size;
                break;
            case REVERT_SIZE://按照从大到小，文件越大优先级越高
                int rSize = cursor.getInt(cursor.getColumnIndex(Column.SIZE));
                if(rSize <= 0) break;
                where = Column.SIZE+"<="+rSize;
                break;
        }
        return where;
    }
}
