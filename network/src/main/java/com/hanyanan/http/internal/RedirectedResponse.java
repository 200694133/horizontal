package com.hanyanan.http.internal;

import com.hanyanan.http.Headers;

/**
 * Created by hanyanan on 2015/5/23.
 */
public class RedirectedResponse {
    private final HttpResponseHeader responseHeader;
    private final int code;
    private final String location;
    private final String msg;

    RedirectedResponse(int code, String msg, String location, HttpResponseHeader responseHeader) {
        this.code = code;
        this.msg = msg;
        this.location = location;
        this.responseHeader = responseHeader;
    }

    RedirectedResponse(int code, String msg, HttpResponseHeader responseHeader){
        this.code = code;
        this.msg = msg;
        this.location = responseHeader.value(Headers.LOCATION.value());
        this.responseHeader = responseHeader;
    }

    public HttpResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public int getCode() {
        return code;
    }

    public String getLocation() {
        return location;
    }

    public String getMsg() {
        return msg;
    }







}
