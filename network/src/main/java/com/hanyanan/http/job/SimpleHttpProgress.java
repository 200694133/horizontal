package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;

/**
 * Created by n550 on 2015/6/13.
 */
class SimpleHttpProgress implements TransportProgress {
    private final HttpRequestJob job;
    private final HttpRequest request;
    private final long position;
    private final long count;
    private final boolean down;
    SimpleHttpProgress(boolean down, HttpRequestJob job, HttpRequest request, long position, long count){
        this.job = job;
        this.request = request;
        this.position = position;
        this.count = count;
        this.down = down;
    }

    @Override
    public boolean isDownloading() {
        return down;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return request;
    }

    @Override
    public long getCurrentPosition() {
        return position;
    }

    @Override
    public long getCount() {
        return count;
    }
}
