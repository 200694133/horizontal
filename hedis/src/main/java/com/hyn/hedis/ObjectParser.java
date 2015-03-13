package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

/**
 * Created by hanyanan on 2015/2/27.
 * Transfer object to byte array and transfer byte array to object.
 */
public interface ObjectParser<T> {
    /**
     * Return the byte array which come from input object.
     * @see #transferToObject(byte[])
     * @param object The data need to transfer to byte array
     */
    public byte[] transferToBlob(T object);

    /**
     * Return the raw object which come from byte array.
     * @see #transferToBlob(Object)
     * @param blob The binary byte array.
     */
    public T transferToObject(byte[] blob)throws HedisException;
}
