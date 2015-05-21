package hyn.com.lib.android.parser;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class IntegerParser extends BaseObjectParser<Integer> {
    @Override
    public byte[] transferToBlob(Integer object) {
        if(null == object) return new byte[0];
        return ByteUtil.intToBytes(object);
    }

    @Override
    public Integer transferToObject(byte[] blob) throws ParseFailedException {
        if(null == blob || 4 != blob.length) throw new ParseFailedException(new IllegalArgumentException());
        return ByteUtil.bytesToInt(blob, 0);
    }

    @Override
    protected int getBinarySize() {
        return 4;
    }
}
