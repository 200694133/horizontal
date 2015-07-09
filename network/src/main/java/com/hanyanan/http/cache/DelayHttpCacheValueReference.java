package com.hanyanan.http.cache;

import com.hanyanan.http.internal.HttpResponse;

import hyn.com.lib.DelayValueReference;

/**
 * Created by hanyanan on 2015/7/9.
 */
public class DelayHttpCacheValueReference implements DelayValueReference<HttpResponse> {
    @Override
    public HttpResponse getValue() {
        return null;
    }
}
