package hyn.com.lib.android.parser;

import java.io.IOException;
import java.io.InputStream;

import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/5/5.
 */
public abstract class BaseObjectParser<T> implements ObjectParser<T> {
    @Override
    public InputStream transferToStream(T object) {
        return IOUtil.bytesToInputStream(transferToBlob(object));
    }

    @Override
    public T transferToObject(byte[] blob) throws ParseFailedException {
        return null;
    }

    @Override
    public T transferToObject(InputStream inputStream, boolean autoClose) throws ParseFailedException {
        int length = getBinarySize();
        try {
            byte[] data = IOUtil.getBytesFromStream(inputStream, length);
            return transferToObject(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ParseFailedException(e);
        }
    }

    protected abstract int getBinarySize();
}
