package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class BooleanParser implements ObjectParser<Boolean> {
    @Override
    public byte[] transferToBlob(Boolean object) {
        if(object) return new byte[]{1};
        return new byte[]{0};
    }

    @Override
    public Boolean transferToObject(byte[] blob) throws HedisException {
        if(null == blob || blob.length<1) throw new HedisException(new IllegalArgumentException());
        if(0==blob[0]) return Boolean.FALSE;
        return Boolean.TRUE;
    }
}
