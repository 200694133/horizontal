package com.hanyanan.http;

import com.hanyanan.http.internal.HttpBinaryResource;
import com.sun.deploy.net.HttpRequest;

/**
 * Created by hanyanan on 2015/5/11.
 */
public final class HttpResponse {
    /** The raw http request */
    private final HttpRequest httpRequest;
    /** Http response header for current request. */
    private final HttpResponseHeader responseHeader;
    /** The real body which store the content response from server. */
    private final HttpBinaryResource bodyResource;
    /** http resonse code */
    private final int code;
    /** http status message */
    private final String msg;


    public HttpBinaryResource body(){
        return bodyResource;
    }
}

