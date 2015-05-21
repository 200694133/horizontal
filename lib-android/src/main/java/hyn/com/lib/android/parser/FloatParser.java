package hyn.com.lib.android.parser;

import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class FloatParser extends BaseObjectParser<Float> {
    @Override
    public byte[] transferToBlob(Float object) {
        return ByteUtil.floatToBytes(object);
    }

    @Override
    public Float transferToObject(byte[] blob) throws ParseFailedException {
        if(null == blob || blob.length < 4) throw new ParseFailedException(new IllegalArgumentException());
        return ByteUtil.bytesToFloat(blob, 0);
    }

    @Override
    protected int getBinarySize() {
        return 4;
    }

}
