package com.hanyanan.http;

import com.hanyanan.http.HttpRequest;

/**
 * Created by hanyanan on 2015/5/21.
 */
public interface TransportProgress {

    public boolean isDownloading();

    public HttpRequest getHttpRequest();

    public long getCurrentPosition();

    public long getCount();
}
