package com.hanyanan.http.internal;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.HttpResponse;

import hyn.com.lib.Asyncable;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/13.
 */
public class HttpExecutor implements Asyncable<HttpRequest, HttpResponse> {
    public static final HttpExecutor sHttpExecutor = new HttpExecutor();

    private HttpExecutor(){}

    @Override
    public HttpResponse run(HttpRequest param) {
        return null;
    }
}
