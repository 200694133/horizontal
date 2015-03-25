package com.hyn.hedis;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

import com.hyn.hedis.exception.HedisException;

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
    public Bitmap transferToObject(byte[] blob) throws HedisException {
        return UiUtil.bytesToBitmap(blob);
    }
}
