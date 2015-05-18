package hyn.com.lib.android.parser;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class ShortParser extends BaseObjectParser<Short> {
    @Override
    public byte[] transferToBlob(Short object) {
        if(null == object) return new byte[0];
        return ByteUtil.shortToBytes(object);
    }

    @Override
    public Short transferToObject(byte[] blob)  {
        if(blob == null || blob.length < 2) return null;
        return ByteUtil.byteArrayToShort(blob);
    }

    @Override
    protected int getBinarySize() {
        return 2;
    }

}
