package com.hanyanan.http.cache;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.job.HttpFingerPrint;

/**
 * Created by hanyanan on 2015/7/9.
 */
public interface HttpSaver {

    public void read(HttpFingerPrint fingerPrint);

    public HttpResponse save(HttpFingerPrint fingerPrint, HttpResponse httpResponse);
}
