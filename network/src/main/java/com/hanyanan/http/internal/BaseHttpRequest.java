package com.hanyanan.http.internal;

import android.support.annotation.Nullable;

import com.hanyanan.http.HttpRequestBody;
import com.hanyanan.http.HttpRequestHeader;
import com.hanyanan.http.Method;
import com.hanyanan.http.Protocol;
import com.hanyanan.http.TrafficStatus;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/13.
 * The main http request.
 */
public class BaseHttpRequest{
    /** http request body, it it's a  */
    @Nullable private final HttpRequestBody requestBody;

    private final HttpRequestHeader requestHeader;

    private final String url;

    private final Method method;

    private final Protocol protocol;
    /** A monitor to record the traffic. */
    private final TrafficStatus trafficStatus;

    private final HttpExecutor httpExecutor;

    public BaseHttpRequest(String url, Method method, Protocol protocol) {
        this.url = url;
        this.method = method;
        this.protocol = protocol;
        this.requestBody = new HttpRequestBody();
        this.requestHeader = new HttpRequestHeader();
        this.trafficStatus = TrafficStatus.creator();
        this.httpExecutor = HttpExecutor.sHttpExecutor;
    }
    public BaseHttpRequest(String url, Method method) {
        this(url, method, Protocol.HTTP_1_1);
    }

    public BaseHttpRequest(String url) {
        this(url, Method.GET, Protocol.HTTP_1_1);
    }

    @Nullable public HttpRequestBody getRequestBody() {
        if(HttpPreconditions.permitsRequestBody(method.toString())) {
            return requestBody;
        }
        return null;
    }

    public HttpRequestHeader getRequestHeader() {
        return requestHeader;
    }

    public final String getUrl() {
        return url;
    }

    public final Method getMethod() {
        return method;
    }

    public final TrafficStatus getTrafficStatus(){
        return trafficStatus;
    }

    public final HttpExecutor getHttpExecutor(){
        return httpExecutor;
    }

    public BaseHttpRequest addCookie(String cookie){
        //TODO
        return this;
    }

    public BaseHttpRequest range(long start, long count){
        //TODO
        return this;
    }
}
