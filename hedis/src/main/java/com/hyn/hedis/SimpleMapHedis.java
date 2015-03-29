package com.hyn.hedis;

import android.content.Context;

import com.hyn.hedis.exception.HedisException;

import java.io.Serializable;

import hyn.com.lib.TimeUtils;

/**
 * Created by hanyanan on 2015/3/9.
 */
public class SimpleMapHedis extends MapHedisImpl {
    public SimpleMapHedis(Context context) {
        super(context);
    }
    public SimpleMapHedis(Context context, String path) {
        super(context, path);
    }

    public Boolean getBoolean(final String key) throws HedisException {
        return get(key, new BooleanParser());
    }
    public Integer getInteger(final String key) throws HedisException {
        return get(key, new IntegerParser());
    }
    public Long getLong(final String key) throws HedisException {
        return get(key, new LongParser());
    }
    public Float getFloat(final String key) throws HedisException {
        return get(key, new FloatParser());
    }
    public Double getDouble(final String key) throws HedisException {
        return get(key, new DoubleParser());
    }
    public byte[] getBytes(final String key) throws HedisException {
        return get(key, new ByteArrayParser());
    }
    public String getString(final String key) throws HedisException {
        return get(key, new StringParser());
    }
    public Serializable getSerializable(final String key) throws HedisException {
        return get(key, new SerializableParser());
    }


    public Boolean putBoolean(final String key, final boolean content) throws HedisException {
        return put(key, content, new BooleanParser(), TimeUtils.getCurrentWallClockTime());
    }
    public Integer putInteger(final String key, final int content) throws HedisException {
        return put(key, content,new IntegerParser(), TimeUtils.getCurrentWallClockTime());
    }
    public Long putLong(final String key, final long content) throws HedisException {
        return put(key, content,new LongParser(), TimeUtils.getCurrentWallClockTime());
    }
    public Float putFloat(final String key, final float content) throws HedisException {
        return put(key, content,new FloatParser(), TimeUtils.getCurrentWallClockTime());
    }
    public Double putDouble(final String key, final double content) throws HedisException {
        return put(key, content,new DoubleParser(), TimeUtils.getCurrentWallClockTime());
    }
    public byte[] putBytes(final String key, final byte[] content) throws HedisException {
        return put(key, content,new ByteArrayParser(), TimeUtils.getCurrentWallClockTime());
    }
    public String putString(final String key, final String content) throws HedisException {
        return put(key, content,new StringParser(), TimeUtils.getCurrentWallClockTime());
    }
    public Serializable putSerializable(final String key, final Serializable content) throws HedisException {
        return put(key, content,new SerializableParser(), TimeUtils.getCurrentWallClockTime());
    }
}
