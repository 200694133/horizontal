package com.hyn.hedis;

import android.content.Context;
import com.hyn.hedis.exception.HedisException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import hyn.com.lib.ThreeTuple;
import hyn.com.lib.TimeUtils;

/**
 * Created by hanyanan on 2015/3/16.
 */
public class SetHedisImpl<T> implements SetHedis<T> {
    protected final LinkedHashMapHedisDataBaseHelper mLinkedHashMapHedisDataBaseHelper;
    protected final String tag;
    protected final ObjectParser<T> objectParser;
    protected final OrderPolicy orderPolicy;
    public SetHedisImpl(Context context, String tag, OrderPolicy orderPolicy, ObjectParser<T> parser){
        mLinkedHashMapHedisDataBaseHelper = LinkedHashMapHedisDataBaseHelper.getInstance(context);
        this.tag = tag;
        objectParser = parser;
        this.orderPolicy = orderPolicy;
    }


    private List<T> parseObjects(Collection<ThreeTuple<String, String,byte[]>> threeTuple){
        if(null == threeTuple || threeTuple.size() <= 0) return null;
        final List<T> result = new ArrayList<>();
        for(ThreeTuple<String, String,byte[]> tuple : threeTuple){
            T t = parseObject(tuple);
            if(null == t) continue;
            result.add(t);
        }
        return result;
    }

    private T parseObject(ThreeTuple<String,String,byte[]> threeTuple){
        if(null == threeTuple || threeTuple.thirdValue==null || threeTuple.thirdValue.length<=0) return null;
        try {
            return getParser().transferToObject(threeTuple.thirdValue);
        } catch (HedisException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OrderPolicy getOrderPolicy() {
        return orderPolicy;
    }

    @Override
    public ObjectParser<T> getParser() {
        return objectParser;
    }

    private long getSystem(){
        if(enableExpireTime()) return TimeUtils.getCurrentWallClockTime();
        return Long.MAX_VALUE/2;
    }

    @Override
    public ThreeTuple<String, String, T> eldest() throws HedisException {
        ThreeTuple<String,String,byte[]> res = mLinkedHashMapHedisDataBaseHelper.eldest(getTag(), getSystem(), getOrderPolicy());
        if(null == res) return null;
        return new ThreeTuple<>(res.firstValue, res.secondValue, getParser().transferToObject(res.thirdValue));
    }

    @Override
    public void put(String key, T content, long expireTimeDelta) throws HedisException {
        if(!enableExpireTime() || expireTimeDelta <= 0){
            mLinkedHashMapHedisDataBaseHelper.put(getTag(), key, getParser().transferToBlob(content),Long.MAX_VALUE);
        }else{
            mLinkedHashMapHedisDataBaseHelper.put(getTag(), key, getParser().transferToBlob(content),
                                                                      TimeUtils.getCurrentWallClockTime() + expireTimeDelta);
        }
    }

    @Override
    public void put(String key, T content) throws HedisException {
        put(key, content, Long.MAX_VALUE/2);
    }

    @Override
    public T replace(String key, T content, long expireTimeDelta) throws HedisException {
        ThreeTuple<String,String,byte[]> res = mLinkedHashMapHedisDataBaseHelper.get(getTag(), key, getSystem());
        put(key, content, expireTimeDelta);
        if(null == res) return null;
        return getParser().transferToObject(res.thirdValue);
    }

    @Override
    public T replace(String key, T content) throws HedisException {
        return replace(key, content, Long.MAX_VALUE/2);
    }

    @Override
    public List<ThreeTuple<String, String, T>> getAll() throws HedisException {
        List<ThreeTuple<String, String,byte[]>> res=mLinkedHashMapHedisDataBaseHelper.getAll(getTag(), getSystem(), getOrderPolicy());
        if(null == res || res.size() <= 0) return null;
        List<ThreeTuple<String, String, T>> out = new ArrayList<>();
        for(ThreeTuple<String, String, byte[]> tuple : res){
            if(null == tuple) continue;
            out.add(new ThreeTuple<String, String, T>(tuple.firstValue, tuple.secondValue, getParser().transferToObject(tuple.thirdValue)));
        }
        return out;
    }

    @Override
    public List<ThreeTuple<String, String, T>> getPage(int pageIndex, int pageCount) throws HedisException {
        List<ThreeTuple<String, String,byte[]>> res=mLinkedHashMapHedisDataBaseHelper.getPage(getTag(),getSystem(),pageIndex, pageCount, getOrderPolicy());
        if(null == res || res.size() <= 0) return null;
        List<ThreeTuple<String, String, T>> out = new ArrayList<>();
        for(ThreeTuple<String, String, byte[]> tuple : res){
            if(null == tuple) continue;
            out.add(new ThreeTuple<String, String, T>(tuple.firstValue, tuple.secondValue, getParser().transferToObject(tuple.thirdValue)));
        }
        return out;
    }

    @Override
    public ThreeTuple<String, String, T> get(String key) throws HedisException {
        ThreeTuple<String,String,byte[]> res = mLinkedHashMapHedisDataBaseHelper.get(getTag(), key, getSystem());
        if(null == res) return null;
        return new ThreeTuple<String, String, T>(res.firstValue, res.secondValue, getParser().transferToObject(res.thirdValue));
    }

    @Override
    public void remove(String key) {
        mLinkedHashMapHedisDataBaseHelper.delete(getTag(), key);
    }

    @Override
    public ThreeTuple<String, String, T> fetch(String key) throws HedisException {
        ThreeTuple<String, String, T> res = get(key);
        remove(key);
        return res;
    }

    @Override
    public void evictAll() {
        //TODO
    }

    @Override
    public int size() {
        return mLinkedHashMapHedisDataBaseHelper.getSize(getTag(), getSystem());
    }

    @Override
    public int maxSize() {
        return 0;
    }

    @Override
    public int count() {
        return mLinkedHashMapHedisDataBaseHelper.getCount(getTag(), getSystem());
    }

    @Override
    public int maxCount() {
        return 0;
    }

    @Override
    public void trimToCount(int maxRowCount) {
        mLinkedHashMapHedisDataBaseHelper.trimCount(getTag(), maxRowCount, getSystem(), getOrderPolicy());
    }

    @Override
    public void trimToSize(int maxSize) {

    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public boolean enableExpireTime() {
        return true;
    }

    @Override
    public void setRemoveListener(OnEntryRemovedListener<T> listener) {

    }

    @Override
    public void dispose() {

    }
}
