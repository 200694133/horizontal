package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class StringParser implements ObjectParser<String> {
    @Override
    public byte[] transferToBlob(String object) {
        if(null == object) return new byte[0];
        return object.getBytes();
    }

    @Override
    public String transferToObject(byte[] blob) throws HedisException {
        if(null == blob || blob.length <= 0) return "";
        return new String(blob);
    }
}