package com.hyn.hedis;

import android.content.Context;

import com.hyn.hedis.exception.HedisException;

import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;

/**
 * Created by hanyanan on 2015/3/9.
 * The map structure as follow:
 * |----------------------------------------------------------  map  ------------------------------------------------------------------|
 * |---- key -----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- map1 ----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- map2 ----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |-----------------------------------------------------------------------------------------------------------------------------------|
 * column key as the primary key
 */
public class MapHedisImpl implements MapHedis {
    private final MapHedisDataBaseHelper mMapHedisDataBaseHelper;

    public MapHedisImpl(Context context){
        mMapHedisDataBaseHelper = new MapHedisDataBaseHelper(context);
    }

    public MapHedisImpl(Context context,String path){
        mMapHedisDataBaseHelper = new MapHedisDataBaseHelper(context, path);
    }

    @Override
    public boolean exits(String key) {
        return mMapHedisDataBaseHelper.exits(key);
    }

    @Override
    public int count(String key) {
        if(exits(key)) return 1;
        return 0;
    }

    @Override
    public boolean empty(String key) {
        return !exits(key);
    }

    @Override
    public <T> T get(String key, ObjectParser<T> objectParser) throws HedisException {
        TwoTuple<String, byte[]> res = mMapHedisDataBaseHelper.get(key);
        if(null == res || res.secondValue == null) return null;
        return objectParser.transferToObject(res.secondValue);
    }

    @Override
    public <T> T put(String key, T content, ObjectParser<T> objectParser, long expireTimeDelta) throws HedisException {
        T res = get(key, objectParser);
        if(null == content) {
            remove(key);
        }else{
            byte[] inputs = objectParser.transferToBlob(content);
            if(null == inputs || inputs.length <= 0){
                remove(key);
            }else {
                long time = TimeUtils.getCurrentWallClockTime();
                mMapHedisDataBaseHelper.put(key, inputs, time+expireTimeDelta, time);
            }
        }
        return res;
    }

    @Override
    public void remove(String key) {
        mMapHedisDataBaseHelper.delete(key);
    }

    @Override
    public String getTag() {
        return null;
    }
}
