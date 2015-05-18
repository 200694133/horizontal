package hyn.com.lib.android.parser;


import hyn.com.datastorage.exception.ParseFailedException;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class BooleanParser extends BaseObjectParser<Boolean> {
    @Override
    public byte[] transferToBlob(Boolean object) {
        if(object) return new byte[]{1};
        return new byte[]{0};
    }

    @Override
    public Boolean transferToObject(byte[] blob) throws ParseFailedException {
        if(null == blob || blob.length<1) throw new ParseFailedException(new IllegalArgumentException());
        if(0==blob[0]) return Boolean.FALSE;
        return Boolean.TRUE;
    }

    @Override
    protected int getBinarySize() {
        return 1;
    }
}
