package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class ByteArrayParser implements ObjectParser<byte[]> {
    @Override
    public byte[] transferToBlob(byte[] object) {
        return object;
    }

    @Override
    public byte[] transferToObject(byte[] blob) throws HedisException {
        return blob;
    }
}
