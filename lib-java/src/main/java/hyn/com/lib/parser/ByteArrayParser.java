package hyn.com.lib.parser;


import java.io.InputStream;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class ByteArrayParser extends BaseObjectParser <byte[]> implements ObjectParser<byte[]> {
    @Override
    public byte[] transferToBlob(byte[] object) {
        return object;
    }

    @Override
    public byte[] transferToObject(byte[] blob) {
        return blob;
    }

    @Override
    public byte[] transferToObject(InputStream inputStream, boolean autoClose) throws ParseFailedException {
        byte[] content = IOUtil.inputStreamToBytes(inputStream);
        if(autoClose) {
            IOUtil.safeClose(inputStream);
        }
        return content;
    }

    @Override
    protected int getBinarySize() {
        return 0;
    }
}
