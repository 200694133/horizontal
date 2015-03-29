package com.hyn.hedis;

import android.content.Context;

import com.hyn.hedis.exception.HedisException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.android.Log;

/**
 * Created by hanyanan on 2015/3/2.
 * The lower value of priority location in the head.
 * 优先级值越小，优先级越高
 */
public class QueueHedisImpl implements QueueHedis {
    private final QueueHedisDataBaseHelper mQueueHedisDataBaseHelper;
    public QueueHedisImpl(Context context){
        mQueueHedisDataBaseHelper = new QueueHedisDataBaseHelper(context);
    }
    public QueueHedisImpl(Context context,String path){
        mQueueHedisDataBaseHelper = new QueueHedisDataBaseHelper(context, path);
    }
    @Override
    public <T> void pushHead(String key, T content, ObjectParser<T> parser, long deltaExpireTime) {
        long priority = -TimeUtils.getCurrentWallClockTime();
        byte[] body = parser.transferToBlob(content);
        mQueueHedisDataBaseHelper.put(key,body,priority,-priority+deltaExpireTime,-priority);
    }

    @Override
    public <T> void pushTail(String key, T content, ObjectParser<T> parser, long deltaExpireTime) {
        long priority = TimeUtils.getCurrentWallClockTime();
        byte[] body = parser.transferToBlob(content);
        mQueueHedisDataBaseHelper.put(key,body,priority,priority+deltaExpireTime,priority);
    }

    @Override
    public <T> T tail(String key, ObjectParser<T> parser) throws HedisException {
        long accessTime = TimeUtils.getCurrentWallClockTime();
        TwoTuple<String,byte[]> res = mQueueHedisDataBaseHelper.getHead(key, accessTime);
        if(null == res || res.secondValue == null || res.secondValue.length<=0) return null;
        T result = parser.transferToObject(res.secondValue);
        return result;
    }

    @Override
    public <T> T head(String key, ObjectParser<T> parser) throws HedisException {
        long accessTime = TimeUtils.getCurrentWallClockTime();
        TwoTuple<String,byte[]> res = mQueueHedisDataBaseHelper.getTail(key, accessTime);
        if(null == res || res.secondValue == null || res.secondValue.length<=0) return null;
        T result = parser.transferToObject(res.secondValue);
        return result;
    }

    @Override
    public <T> T takeTail(String key, ObjectParser<T> parser) throws HedisException {
        long accessTime = TimeUtils.getCurrentWallClockTime();
        TwoTuple<String,byte[]> res = mQueueHedisDataBaseHelper.getTail(key, accessTime);
        if(null == res || res.secondValue == null || res.secondValue.length<=0) return null;
        T result = parser.transferToObject(res.secondValue);
        mQueueHedisDataBaseHelper.deleteTailByPriority(key);
        return result;
    }

    @Override
    public <T> T takeHead(String key, ObjectParser<T> parser) throws HedisException {
        long accessTime = TimeUtils.getCurrentWallClockTime();
        TwoTuple<String,byte[]> res = mQueueHedisDataBaseHelper.getHead(key, accessTime);
        if(null == res || res.secondValue == null || res.secondValue.length<=0) return null;
        T result = parser.transferToObject(res.secondValue);
        mQueueHedisDataBaseHelper.deleteHeadByPriority(key);
        return result;
    }

    @Override
    public <T> Collection<T> getAll(String key, ObjectParser<T> parser) throws HedisException {
        long accessTime = TimeUtils.getCurrentWallClockTime();
        final Collection<TwoTuple<String,byte[]>> res = mQueueHedisDataBaseHelper.queryAll(key, 0);
        if(null == res || res.size() <= 0) return null;
        return parseResult(res, parser);
    }

    @Override
    public void deleteTail(String key) {
        mQueueHedisDataBaseHelper.deleteTailByPriority(key);
    }

    @Override
    public void deleteFirst(String key) {
        mQueueHedisDataBaseHelper.deleteHeadByPriority(key);
    }

    @Override
    public <T> Collection<T> trimCount(String key, int maxRowCount, ObjectParser<T> parser) throws HedisException {
        Collection<TwoTuple<String,byte[]>> deleteList = mQueueHedisDataBaseHelper.trimCountWithResult(key, maxRowCount);
        if(null == deleteList || deleteList.size() <= 0) return null;
        return parseResult(deleteList, parser);
    }

    @Override
    public void trimCountSilence(String key, int maxRowCount) {
        mQueueHedisDataBaseHelper.trimCountSilence(key,maxRowCount);
    }

    @Override
    public <T> Collection<T> trimSize(String key, int maxSize, ObjectParser<T> parser) throws HedisException {
        int size = mQueueHedisDataBaseHelper.getGroupSize(key);
        if(size <= maxSize) return null;
        Collection<TwoTuple<Integer,Integer>> all = mQueueHedisDataBaseHelper.querySimple(key);
        int currentSize = 0;
        List<Integer> deleteRequire = new ArrayList<>();
        for(TwoTuple<Integer,Integer> tuple : all){
            if(null == all || tuple.firstValue == null || tuple.secondValue == null) continue;
            currentSize += tuple.secondValue;
            if(currentSize > maxSize) {
                deleteRequire.add(tuple.firstValue);
            }
        }

        final Collection<TwoTuple<String,byte[]>> res = mQueueHedisDataBaseHelper.deleteByIdsWithResult(key,deleteRequire);
        if(null == res || res.size() <= 0) return null;
        return parseResult(res, parser);
    }


    @Override
    public void trimSizeSilence(String key, int maxSize) {
        int size = mQueueHedisDataBaseHelper.getGroupSize(key);
        if(size <= maxSize) return ;
        Collection<TwoTuple<Integer,Integer>> all = mQueueHedisDataBaseHelper.querySimple(key);
        int currentSize = 0;
        List<Integer> deleteRequire = new ArrayList<>();
        for(TwoTuple<Integer,Integer> tuple : all){
            if(null == all || tuple.firstValue == null || tuple.secondValue == null) continue;
            currentSize += tuple.secondValue;
            if(currentSize > maxSize) {
                deleteRequire.add(tuple.firstValue);
            }
        }
        mQueueHedisDataBaseHelper.deleteByIdsSilence(deleteRequire);
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    @Override
    public void dispose() {
        mQueueHedisDataBaseHelper.close();
        Log.d("QueueHedisImpl dispose");
    }

    @Override
    public boolean exits(String key) {
        return count(key) > 0;
    }

    @Override
    public int count(String key) {
        return mQueueHedisDataBaseHelper.getGroupCount(key);
    }

    @Override
    public boolean empty(String key) {
        return count(key) <= 0;
    }

    @Override
    public <T> T get(String key, ObjectParser<T> objectParser) throws HedisException {
        return head(key, objectParser);
    }

    @Override
    public <T> T put(String key, T content, ObjectParser<T> objectParser, long expireTimeDelta) throws HedisException {
        pushTail(key, content, objectParser, expireTimeDelta);
        return content;
    }

    @Override
    public void remove(String key) {
        mQueueHedisDataBaseHelper.deleteAll(key);
    }

    @Override
    public String getTag() {
        return null;
    }

    public static <T> List<T> parseResult(Collection<TwoTuple<String,byte[]>> input, ObjectParser<T> parser) throws HedisException {
        final Collection<TwoTuple<String,byte[]>> res = input;
        if(null == res || res.size() <= 0) return null;
        final ArrayList<T> result = new ArrayList<T>();
        for(TwoTuple<String,byte[]> element : res) {
            if(null == element || element.secondValue==null||element.secondValue.length<=0) continue;
            result.add(parser.transferToObject(element.secondValue));
        }
        return result;
    }
}
