package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class LongParser implements ObjectParser<Long> {
    @Override
    public byte[] transferToBlob(Long object) {
        if(null == object) return new byte[0];
        return ByteUtil.longToBytes(object);
    }

    @Override
    public Long transferToObject(byte[] blob) throws HedisException {
        if(null == blob || blob.length < 8) return null;
        return ByteUtil.bytesToLong(blob);
    }
}
