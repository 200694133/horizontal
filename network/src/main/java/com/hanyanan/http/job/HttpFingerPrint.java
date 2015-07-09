package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;

import java.util.concurrent.atomic.AtomicInteger;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/6/12.
 */
public class HttpFingerPrint implements Fingerprint {
    private final static AtomicInteger sSequenceGenerator = new AtomicInteger(0);
    private final HttpRequest request;
    public HttpFingerPrint(HttpRequest request) {
        this.request = request;
    }
    @Override
    public String fingerprint() {
        return "";
    }
}
