package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/6/12.
 */
public class HttpFingerPrint implements Fingerprint {
    private final HttpRequest request;
    public HttpFingerPrint(HttpRequest request) {
        this.request = request;
    }
    @Override
    public String fingerprint() {
        return ValueUtil.md5_16(request.getUrl());
    }
}
