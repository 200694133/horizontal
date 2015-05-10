package hyn.com.datastorage.parser;

import java.io.InputStream;
import java.io.Serializable;

import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.ByteUtil;
import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class SerializableParser extends BaseObjectParser<Serializable> {
    @Override
    public byte[] transferToBlob(Serializable object) {
        if(null == object){
            return new byte[0];
        }else {
            return ByteUtil.serializableToBytes(object);
        }
    }

    @Override
    public Serializable transferToObject(byte[] blob)  {
        if(null == blob || blob.length == 0) return null;
        return (Serializable)ByteUtil.bytesToObject(blob);
    }

    @Override
    public Serializable transferToObject(InputStream inputStream, boolean autoClose) throws ParseFailedException {
        byte[] buff = IOUtil.inputStreamToBytes(inputStream);

        if(autoClose) {
            IOUtil.safeClose(inputStream);
        }

        return transferToObject(buff);
    }

    @Override
    protected int getBinarySize() {
        return 0;
    }
}
