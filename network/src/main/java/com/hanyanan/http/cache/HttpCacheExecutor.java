package com.hanyanan.http.cache;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpResponse;

import hyn.com.lib.DelayValueReference;

/**
 * Created by hanyanan on 2015/7/9.
 *
 * A interface when get data from cache.
 */
public interface HttpCacheExecutor {

    /**
     * Miss current request.
     * @param httpRequest
     */
    public void missCache(HttpRequest httpRequest);

    /**
     *
     * @param httpRequest
     * @return
     */
    public DelayValueReference<HttpResponse> hitExpiredCache(HttpRequest httpRequest);

    /**
     *
     * @param httpRequest
     * @return
     */
    public DelayValueReference<HttpResponse> hitCache(HttpRequest httpRequest);
}
