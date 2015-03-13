package com.hyn.hedis;

import android.content.Context;

import com.hyn.hedis.exception.HedisException;

import java.io.Serializable;
import java.util.Collection;

import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class SimpleQueueHedis extends QueueHedisImpl {
    public SimpleQueueHedis(Context context) {
        super(context);
    }

    public SimpleQueueHedis(Context context, String tag) {
        super(context, tag);
    }

    public void pushHead(final String key, int data){
        pushHead(key, Integer.valueOf(data), new IntegerParser(), Long.MAX_VALUE);
    }

    public void pushHead(final String key, long data){
        pushHead(key, Long.valueOf(data), new LongParser(), Long.MAX_VALUE);
    }

    public void pushHead(final String key, float data){
        pushHead(key, Float.valueOf(data), new FloatParser(), Long.MAX_VALUE);
    }

    public void pushHead(final String key, double data){
        pushHead(key, Double.valueOf(data), new DoubleParser(), Long.MAX_VALUE);
    }

    public void pushHead(final String key, short data){
        pushHead(key, Short.valueOf(data), new ShortParser(), Long.MAX_VALUE);
    }

    public void pushHead(final String key, byte[] data){
        pushHead(key, data, new ByteArrayParser(), Long.MAX_VALUE);
    }
    public void pushHead(final String key, boolean data){
        pushHead(key, Boolean.valueOf(data), new BooleanParser(), Long.MAX_VALUE);
    }
    public void pushHead(final String key, String data){
        pushHead(key, data, new StringParser(), Long.MAX_VALUE);
    }
    public void pushHead(final String key, Serializable data){
        pushHead(key, data, new SerializableParser(), Long.MAX_VALUE);
    }
    public void pushTail(final String key, int data){
        pushTail(key, Integer.valueOf(data), new IntegerParser(), Long.MAX_VALUE);
    }

    public void pushTail(final String key, long data){
        pushTail(key, Long.valueOf(data), new LongParser(), Long.MAX_VALUE);
    }

    public void pushTail(final String key, float data){
        pushTail(key, Float.valueOf(data), new FloatParser(), Long.MAX_VALUE);
    }

    public void pushTail(final String key, double data){
        pushTail(key, Double.valueOf(data), new DoubleParser(), Long.MAX_VALUE);
    }

    public void pushTail(final String key, short data){
        pushTail(key, Short.valueOf(data), new ShortParser(), Long.MAX_VALUE);
    }

    public void pushTail(final String key, byte[] data){
        pushTail(key, data, new ByteArrayParser(), Long.MAX_VALUE);
    }
    public void pushTail(final String key, boolean data){
        pushTail(key, Boolean.valueOf(data), new BooleanParser(), Long.MAX_VALUE);
    }
    public void pushTail(final String key, String data){
        pushTail(key, data, new StringParser(), Long.MAX_VALUE);
    }
    public void pushTail(final String key, Serializable data){
        pushTail(key, data, new SerializableParser(), Long.MAX_VALUE);
    }

    public Integer tailInt(String key) throws HedisException {
        return tail(key, new IntegerParser());
    }
    public Long tailLong(String key) throws HedisException {
        return tail(key, new LongParser());
    }
    public Float tailFloat(String key) throws HedisException {
        return tail(key, new FloatParser());
    }
    public Double tailDouble(String key) throws HedisException {
        return tail(key, new DoubleParser());
    }
    public Short tailShort(String key) throws HedisException {
        return tail(key, new ShortParser());
    }
    public Boolean tailBoolean(String key) throws HedisException {
        return tail(key, new BooleanParser());
    }
    public String tailString(String key) throws HedisException {
        return tail(key, new StringParser());
    }
    public Serializable tailSerializable(String key) throws HedisException {
        return tail(key, new SerializableParser());
    }
    public byte[] tailBytes(String key) throws HedisException {
        return tail(key, new ByteArrayParser());
    }

    public Integer headInt(String key) throws HedisException {
        return head(key, new IntegerParser());
    }
    public Long headLong(String key) throws HedisException {
        return head(key, new LongParser());
    }
    public Float headFloat(String key) throws HedisException {
        return head(key, new FloatParser());
    }
    public Double headDouble(String key) throws HedisException {
        return head(key, new DoubleParser());
    }
    public Short headShort(String key) throws HedisException {
        return head(key, new ShortParser());
    }
    public Boolean headBoolean(String key) throws HedisException {
        return head(key, new BooleanParser());
    }
    public String headString(String key) throws HedisException {
        return head(key, new StringParser());
    }
    public Serializable headSerializable(String key) throws HedisException {
        return head(key, new SerializableParser());
    }
    public byte[] headBytes(String key) throws HedisException {
        return head(key, new ByteArrayParser());
    }


    public Integer takeHeadInt(String key) throws HedisException {
        return takeHead(key, new IntegerParser());
    }
    public Long takeHeadLong(String key) throws HedisException {
        return takeHead(key, new LongParser());
    }
    public Float takeHeadFloat(String key) throws HedisException {
        return takeHead(key, new FloatParser());
    }
    public Double takeHeadDouble(String key) throws HedisException {
        return takeHead(key, new DoubleParser());
    }
    public Short takeHeadShort(String key) throws HedisException {
        return takeHead(key, new ShortParser());
    }
    public Boolean takeHeadBoolean(String key) throws HedisException {
        return takeHead(key, new BooleanParser());
    }
    public String takeHeadString(String key) throws HedisException {
        return takeHead(key, new StringParser());
    }
    public Serializable takeHeadSerializable(String key) throws HedisException {
        return takeHead(key, new SerializableParser());
    }
    public byte[] takeHeadBytes(String key) throws HedisException {
        return takeHead(key, new ByteArrayParser());
    }

    public Integer takeTailInt(String key) throws HedisException {
        return takeTail(key, new IntegerParser());
    }
    public Long takeTailLong(String key) throws HedisException {
        return takeTail(key, new LongParser());
    }
    public Float takeTailFloat(String key) throws HedisException {
        return takeTail(key, new FloatParser());
    }
    public Double takeTailDouble(String key) throws HedisException {
        return takeTail(key, new DoubleParser());
    }
    public Short takeTailShort(String key) throws HedisException {
        return takeTail(key, new ShortParser());
    }
    public Boolean takeTailBoolean(String key) throws HedisException {
        return takeTail(key, new BooleanParser());
    }
    public String takeTailString(String key) throws HedisException {
        return takeTail(key, new StringParser());
    }
    public Serializable takeTailSerializable(String key) throws HedisException {
        return takeTail(key, new SerializableParser());
    }
    public byte[] takeTailBytes(String key) throws HedisException {
        return takeTail(key, new ByteArrayParser());
    }

    public Collection<Integer> trimSizeInteger(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new IntegerParser());
    }
    public Collection<Long> trimSizeLong(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new LongParser());
    }
    public Collection<Short> trimSizeShort(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new ShortParser());
    }
    public Collection<Float> trimSizeFloat(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new FloatParser());
    }
    public Collection<Double> trimSizeDouble(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new DoubleParser());
    }
    public Collection<Boolean> trimSizeBoolean(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new BooleanParser());
    }
    public Collection<byte[]> trimSizeBytes(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new ByteArrayParser());
    }
    public Collection<String> trimSizeString(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new StringParser());
    }
    public Collection<Serializable> trimSizeSerializable(String key, int maxSize) throws HedisException {
        return trimSize(key, maxSize,new SerializableParser());
    }

    public Collection<Integer> trimCountInteger(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new IntegerParser());
    }
    public Collection<Long> trimCountLong(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new LongParser());
    }
    public Collection<Short> trimCountShort(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new ShortParser());
    }
    public Collection<Float> trimCountFloat(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new FloatParser());
    }
    public Collection<Double> trimCountDouble(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new DoubleParser());
    }
    public Collection<Boolean> trimCountBoolean(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new BooleanParser());
    }
    public Collection<byte[]> trimCountBytes(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new ByteArrayParser());
    }
    public Collection<String> trimCountString(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new StringParser());
    }
    public Collection<Serializable> trimCountSerializable(String key, int maxCount) throws HedisException {
        return trimCount(key, maxCount,new SerializableParser());
    }

    public Integer getInt(String key) throws HedisException {
        return get(key, new IntegerParser());
    }
    public Long getLong(String key) throws HedisException {
        return get(key, new LongParser());
    }
    public Float getFloat(String key) throws HedisException {
        return get(key, new FloatParser());
    }
    public Double getDouble(String key) throws HedisException {
        return get(key, new DoubleParser());
    }
    public Short getShort(String key) throws HedisException {
        return get(key, new ShortParser());
    }
    public Boolean getBoolean(String key) throws HedisException {
        return get(key, new BooleanParser());
    }
    public String getString(String key) throws HedisException {
        return get(key, new StringParser());
    }
    public Serializable getSerializable(String key) throws HedisException {
        return get(key, new SerializableParser());
    }
    public byte[] getBytes(String key) throws HedisException {
        return get(key, new ByteArrayParser());
    }

    public Collection<Integer> getAllInteger(String key) throws HedisException {
        return getAll(key, new IntegerParser());
    }
    public Collection<Long> getAllLong(String key) throws HedisException {
        return getAll(key, new LongParser());
    }
    public Collection<Short> getAllShort(String key) throws HedisException {
        return getAll(key, new ShortParser());
    }
    public Collection<Float> getAllFloat(String key) throws HedisException {
        return getAll(key, new FloatParser());
    }
    public Collection<Double> getAllDouble(String key) throws HedisException {
        return getAll(key, new DoubleParser());
    }
    public Collection<Boolean> getAllBoolean(String key) throws HedisException {
        return getAll(key, new BooleanParser());
    }
    public Collection<byte[]> getAllBytes(String key) throws HedisException {
        return getAll(key, new ByteArrayParser());
    }
    public Collection<String> getAllString(String key) throws HedisException {
        return getAll(key, new StringParser());
    }
    public Collection<Serializable> getAllSerializable(String key) throws HedisException {
        return getAll(key, new SerializableParser());
    }
}
