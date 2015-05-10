package hyn.com.datastorage.parser;


import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.ByteUtil;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class DoubleParser extends BaseObjectParser<Double> {
    @Override
    public byte[] transferToBlob(Double object) {
        if(null == object)
            return new byte[0];
        return ByteUtil.doubleToBytes(object);
    }

    @Override
    public Double transferToObject(byte[] blob) throws ParseFailedException {
        if(null == blob || blob.length < 8) throw new ParseFailedException(new IllegalArgumentException());
        return ByteUtil.bytesToDouble(blob);
    }

    @Override
    protected int getBinarySize() {
        return 8;
    }

}
