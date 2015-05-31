package hyn.com.datastorage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.parser.ObjectParser;
import hyn.com.lib.IOUtil;
import hyn.com.datastorage.db.BaseSQLiteOpenHelper.Column;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.android.logging.Log;
import hyn.com.lib.binaryresource.BinaryResource;
import hyn.com.lib.binaryresource.ByteArrayBinaryResource;

/**
 * Created by hanyanan on 2015/4/1.
 * Implement basic map structure.
 */
public class MapDataStorageImpl implements MapStructureDataStorage {
    private static final String TABLE_NAME = BasicDataBaseHelper.MAP_TABLE_NAME;
    private final BasicDataBaseHelper basicDataBaseHelper;
    private final AtomicBoolean disposed = new AtomicBoolean(false);
    public MapDataStorageImpl(BasicDataBaseHelper basicDataBaseHelper) {
        this.basicDataBaseHelper = basicDataBaseHelper;
    }

    private void checkState() {
        if(disposed.get()){
            throw new IllegalStateException("");
        }
    }

    private String getEncodeKey(String rawKey){
        return ValueUtil.md5_16(rawKey);
    }

    @Override
    public <T> void put(String key, T content, ObjectParser<T> parser) {
        put(key, content, parser, Long.MAX_VALUE);
    }

    @Override
    public <T> void put(String key, InputStream inputStream) {
        put(key,inputStream, Long.MAX_VALUE);
    }

    @Override
    public <T> void put(String key, T content, ObjectParser<T> parser, Long expireTime) {
        checkState();
        byte[] body = parser.transferToBlob(content);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.RAW_KEY, key);
        contentValues.put(Column.KEY, getEncodeKey(key));
        contentValues.put(Column.CONTENT, body);
        contentValues.put(Column.EXPIRE_TIME, expireTime==null?Long.MAX_VALUE:expireTime.longValue());
        basicDataBaseHelper.getWritableDatabase().insertWithOnConflict( TABLE_NAME, null,
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public <T> void put(String key, InputStream inputStream, Long expireTime) {
        checkState();
        byte[] body = IOUtil.inputStreamToBytes(inputStream);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.RAW_KEY, key);
        contentValues.put(Column.KEY, getEncodeKey(key));
        contentValues.put(Column.CONTENT, body);
        contentValues.put(Column.EXPIRE_TIME, expireTime==null?Long.MAX_VALUE:expireTime.longValue());
        basicDataBaseHelper.getWritableDatabase().insertWithOnConflict(TABLE_NAME, null,
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public <T> T get(String key, ObjectParser<T> parser) throws ParseFailedException {
        checkState();
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n",Column.KEY, getEncodeKey(key), Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n",Column.KEY, getEncodeKey(key)));
        }
        String querySql = sb.toString();
        Log.d(TABLE_NAME, "MapDataStorage query run sql " + querySql);
        Cursor cursor = basicDataBaseHelper.getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return null;
        }
        byte[] res = cursor.getBlob(cursor.getColumnIndex(Column.CONTENT));
        IOUtil.safeClose(cursor);
        return parser.transferToObject(res);
    }

    @Override
    public BinaryResource get(String key) {
        checkState();
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n",Column.KEY, getEncodeKey(key), Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n",Column.KEY, getEncodeKey(key)));
        }
        String querySql = sb.toString();
        Log.d(TABLE_NAME,"MapDataStorage query run sql " + querySql);
        Cursor cursor = basicDataBaseHelper.getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return null;
        }
        byte[] res = cursor.getBlob(cursor.getColumnIndex(Column.CONTENT));
        IOUtil.safeClose(cursor);
        if(null == res || res.length<=0) return null;
        return new ByteArrayBinaryResource(res);
    }

    @Override
    public void remove(String key) {
        checkState();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s'\n",Column.KEY, getEncodeKey(key)));
        String sql = sb.toString();
        Log.d(TABLE_NAME,"MapDataStorage remove sql " + sql);
        basicDataBaseHelper.getWritableDatabase().execSQL(sql);
    }

    @Override
    public void clear() {
        checkState();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        String sql = sb.toString();
        Log.d(TABLE_NAME,"MapDataStorage clear sql " + sql);
        basicDataBaseHelper.getWritableDatabase().execSQL(sql);
    }

    @Override
    public void clearTrash() {
        checkState();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DELETE FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s<=%s\n",Column.EXPIRE_TIME, TimeUtils.getCurrentWallClockTime()));
        String sql = sb.toString();
        Log.d(TABLE_NAME,"MapDataStorage clearTrash sql " + sql);
        basicDataBaseHelper.getWritableDatabase().execSQL(sql);
    }

    public boolean isExists(String key){
        checkState();
        long systemTime = TimeUtils.getCurrentWallClockTime();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.CONTENT));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        if(enableExpireRestriction()){
            sb.append(String.format("WHERE %s='%s' AND %s>%s\n",Column.KEY, getEncodeKey(key), Column.EXPIRE_TIME, systemTime));
        }else{
            sb.append(String.format("WHERE %s='%s'\n",Column.KEY, getEncodeKey(key)));
        }
        String querySql = sb.toString();
        Log.d(TABLE_NAME,"MapDataStorage query run sql " + querySql);
        Cursor cursor = basicDataBaseHelper.getReadableDatabase().rawQuery(querySql, null);
        if(null == cursor || !cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return false;
        }
        IOUtil.safeClose(cursor);
        return true;
    }

    @Override
    public boolean isExpired(String key) {
        checkState();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s\n", Column.EXPIRE_TIME));
        sb.append(String.format("FROM %s\n", TABLE_NAME));
        sb.append(String.format("WHERE %s='%s' AND %s>%s\n",Column.KEY, getEncodeKey(key),
                Column.EXPIRE_TIME, TimeUtils.getCurrentWallClockTime()));
        String querySql = sb.toString();
        Cursor cursor = basicDataBaseHelper.getReadableDatabase().rawQuery(querySql, null);
        if(null != cursor && cursor.moveToFirst()) {
            IOUtil.safeClose(cursor);
            return true;
        }
        IOUtil.safeClose(cursor);
        return false;
    }

    @Override
    public boolean enableExpireRestriction() {
        return false;
    }

    @Override
    public void dispose() {
        disposed.set(true);
    }
}
