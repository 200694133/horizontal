package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class DoubleParser implements ObjectParser<Double> {
    @Override
    public byte[] transferToBlob(Double object) {
        if(null == object)
            return new byte[0];
        return ByteUtil.doubleToBytes(object);
    }

    @Override
    public Double transferToObject(byte[] blob) throws HedisException {
        if(null == blob || blob.length < 8) throw new HedisException(new IllegalArgumentException());
        return ByteUtil.bytesToDouble(blob);
    }
}
