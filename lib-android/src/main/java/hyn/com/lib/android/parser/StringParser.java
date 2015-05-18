package hyn.com.lib.android.parser;

import java.io.InputStream;

import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class StringParser extends BaseObjectParser<String> {
    @Override
    public byte[] transferToBlob(String object) {
        if(null == object) return new byte[0];
        return object.getBytes();
    }

    @Override
    public String transferToObject(byte[] blob) {
        if(null == blob || blob.length <= 0) return "";
        return new String(blob);
    }

    @Override
    public String transferToObject(InputStream inputStream, boolean autoClose) throws ParseFailedException {
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