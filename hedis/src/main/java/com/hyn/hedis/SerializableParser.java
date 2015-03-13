package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import java.io.Serializable;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class SerializableParser implements ObjectParser<Serializable> {
    @Override
    public byte[] transferToBlob(Serializable object) {
        if(null == object){
            return new byte[0];
        }else {
            return ByteUtil.serializableToBytes(object);
        }
    }

    @Override
    public Serializable transferToObject(byte[] blob) throws HedisException {
        if(null == blob || blob.length == 0) return null;
        return (Serializable)ByteUtil.bytesToObject(blob);
    }
}
