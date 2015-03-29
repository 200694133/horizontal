package com.hyn.hedis;

import android.content.Context;
import android.os.Environment;

import java.util.WeakHashMap;

/**
 * Created by hanyanan on 2015/3/26.
 */
public class HedisManager {
    private static final String LINKED_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/linked.db";
    private static final String MAP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/map.db";
    private static final String QUEUE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/queue.db";

    private static HedisManager sHedisManager = null;

    public static synchronized HedisManager getInstance(){
        if(null != sHedisManager) {
            return sHedisManager;
        }
        sHedisManager = new HedisManager();
        return sHedisManager;
    }

    private SimpleQueueHedis queueHedis = null;
//    private WeakHashMap<String,LruSetHedis> setHedisMap = new WeakHashMap<>();
    private SimpleMapHedis mapHedis = null;
    private LinkedHashMapHedisDataBaseHelper linkedHashMapHedisDataBaseHelper = null;
    private FastLinkedHashMapDataBaseHelper fastLinkedHashMapDataBaseHelper = null;
    private HedisManager() {
        //TODO
    }

    public synchronized void init(Context context, String dir){
        if(null == queueHedis){
            queueHedis = new SimpleQueueHedis(context,dir+"/queue.db");
        }

        if(null == mapHedis){
            mapHedis = new SimpleMapHedis(context, dir+"/map.db");
        }
        if(null == linkedHashMapHedisDataBaseHelper){
            linkedHashMapHedisDataBaseHelper = new LinkedHashMapHedisDataBaseHelper(context, dir+"/linked.db");
        }
        if(null == fastLinkedHashMapDataBaseHelper){
            fastLinkedHashMapDataBaseHelper = new FastLinkedHashMapDataBaseHelper(context);
        }
    }

    public synchronized void init(Context context){
        if(null == queueHedis){
            queueHedis = new SimpleQueueHedis(context,QUEUE_PATH);
        }

        if(null == mapHedis){
            mapHedis = new SimpleMapHedis(context, MAP_PATH);
        }

        if(null == linkedHashMapHedisDataBaseHelper){
            linkedHashMapHedisDataBaseHelper = new LinkedHashMapHedisDataBaseHelper(context, LINKED_PATH);
        }
    }


    public synchronized <T> LruSetHedis<T> getLruHedis(String tag, ObjectParser<T> parser){
        return new LruSetHedis(linkedHashMapHedisDataBaseHelper, parser, tag);
    }


    public synchronized <T> SetHedis<T> getSetHedis(String tag, ObjectParser<T> parser, OrderPolicy orderPolicy, boolean supportTimeRestriction){
        if(orderPolicy == OrderPolicy.LRU && false == supportTimeRestriction){
            return getLruHedis(tag, parser);
        }
        return new BaseSetHedisImpl<T>(tag, fastLinkedHashMapDataBaseHelper, linkedHashMapHedisDataBaseHelper, parser, orderPolicy, supportTimeRestriction);
    }

    public SimpleQueueHedis getQueueHedis(){
        return queueHedis;
    }

    public SimpleMapHedis getMapHedis(){
        return mapHedis;
    }



    public synchronized void dispose(){
        //TODO
    }
}
