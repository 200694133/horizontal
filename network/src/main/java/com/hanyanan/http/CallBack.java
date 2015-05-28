package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/5/21.
 */
public interface CallBack {
    void onUploadProgress(HttpRequest request, long curr, long size);

    void onTransportProgress(HttpRequest request, long pos, long size);
}
