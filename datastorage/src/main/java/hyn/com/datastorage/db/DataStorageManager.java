package hyn.com.datastorage.db;

import android.content.Context;

import hyn.com.datastorage.exception.TypeConflictException;
import hyn.com.lib.parser.ObjectParser;

/**
 * Created by hanyanan on 2015/4/1.
 */
public class DataStorageManager {
    private final BasicDataBaseHelper basicDataBaseHelper;
    private static DataStorageManager sInstance;
//    private final WeakHashMap<String, >
    //single instance mode
    public synchronized static DataStorageManager getInstance(Context context){
        if(null == sInstance) {
            sInstance = new DataStorageManager(context);
        }
        return sInstance;
    }

    private DataStorageManager(Context context){
        basicDataBaseHelper = new BasicDataBaseHelper(context);
    }

    public <T> OrderStructureDataStorage<T> getOrderStorage(Context context,String tag,
                  ObjectParser<T> parser, OrderStructureDataStorage.OrderPolicy orderPolicy) throws TypeConflictException {
        FastOrderPropertyAttacher attacher = new FastOrderPropertyAttacher(context, tag);
        attacher.sync(basicDataBaseHelper);
        OrderDataStorageImpl<T> impl = new OrderDataStorageImpl<T>(tag, parser, orderPolicy, attacher, basicDataBaseHelper);
        return impl;
    }

}
