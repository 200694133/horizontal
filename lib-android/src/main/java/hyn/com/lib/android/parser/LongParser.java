package hyn.com.lib.android.parser;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class LongParser extends BaseObjectParser<Long> {
    @Override
    public byte[] transferToBlob(Long object) {
        if(null == object) return new byte[0];
        return ByteUtil.longToBytes(object);
    }

    @Override
    public Long transferToObject(byte[] blob) {
        if(null == blob || blob.length < 8) return null;
        return ByteUtil.bytesToLong(blob);
    }

    @Override
    protected int getBinarySize() {
        return 8;
    }

}
