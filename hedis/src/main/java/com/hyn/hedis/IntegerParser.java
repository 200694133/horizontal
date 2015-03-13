package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class IntegerParser implements ObjectParser<Integer> {
    @Override
    public byte[] transferToBlob(Integer object) {
        if(null == object) return new byte[0];
        return ByteUtil.intToBytes(object);
    }

    @Override
    public Integer transferToObject(byte[] blob) throws HedisException {
        if(null == blob || 4 != blob.length) throw new HedisException(new IllegalArgumentException());
        return ByteUtil.bytesToInt(blob, 0);
    }
}
