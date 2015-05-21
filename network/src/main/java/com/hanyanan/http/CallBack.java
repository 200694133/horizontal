package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/5/21.
 */
public interface CallBack {
    void onUploadProgress(long curr, long size);

    void onTransportProgress(long pos, long size);
}
