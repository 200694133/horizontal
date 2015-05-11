package com.hanyanan.http;

import java.util.ArrayList;
import java.util.List;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/11.
 */
public final class HttpResponse {
    /** The raw http request */
    private final HttpRequest httpRequest;
    /** Http response headers, it contain the redirect response header. */
    private final List<HttpResponseHeader> responseHeaders = new ArrayList<HttpResponseHeader>();
    /** The real body which store the content response from server. */
    private final BinaryResource bodyResource;
    /** http resonse code */
    private final int code;
    /** http status message */
    private final String msg;
    /** http protocol */
    private final Protocol protocol;


    public BinaryResource body(){
        return bodyResource;
    }
}

