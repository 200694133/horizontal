package hyn.com.datastorage.parser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

import hyn.com.datastorage.db.ObjectParser;
import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.IOUtil;
import hyn.com.lib.Preconditions;
import hyn.com.lib.android.UiUtil;

/**
 * Created by hanyanan on 2015/3/25.
 */
public class BitmapParser implements ObjectParser<Bitmap> {
    @Override
    public byte[] transferToBlob(Bitmap object) {
        return UiUtil.bitmapToBytes(object);
    }

    @Override
    public InputStream transferToStream(Bitmap object) {
        Preconditions.checkNotNull(object);
        //TODO
        return null;
    }

    @Override
    public Bitmap transferToObject(byte[] blob) {
        return UiUtil.bytesToBitmap(blob);
    }

    @Override
    public Bitmap transferToObject(InputStream inputStream, boolean autoClose) throws ParseFailedException {
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if(autoClose) {
            IOUtil.safeClose(inputStream);
        }
        return bitmap;
    }
}