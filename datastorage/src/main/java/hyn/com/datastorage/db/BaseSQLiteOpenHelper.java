package hyn.com.datastorage.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * Created by hanyanan on 2015/2/28.
 */
public abstract class BaseSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/data.db";
    public BaseSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class Column{
        public static final String TAG = "my_tag";
        public static final String KEY = "my_key";
        public static final String ID = "id";
        public static final String RAW_TAG = "raw_tag";
        public static final String RAW_KEY = "raw_key";
        public static final String PRIORITY = "priority";
        public static final String SIZE = "my_size";
        public static final String LAST_ACCESS_TIME = "accessTime";
        public static final String MODIFY_TIME= "modifyTime";
        public static final String CREATE_TIME = "createTime";
        public static final String EXPIRE_TIME = "expireTime";
        public static final String CONTENT = "content";
    }
}
