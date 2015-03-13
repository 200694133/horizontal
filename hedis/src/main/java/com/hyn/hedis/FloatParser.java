package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class FloatParser implements ObjectParser<Float> {
    @Override
    public byte[] transferToBlob(Float object) {
        return ByteUtil.floatToBytes(object);
    }

    @Override
    public Float transferToObject(byte[] blob) throws HedisException {
        if(null == blob || blob.length < 4) throw new HedisException(new IllegalArgumentException());
        return ByteUtil.bytesToFloat(blob, 0);
    }
}
