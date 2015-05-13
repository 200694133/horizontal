package com.hanyanan.http.internal;

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
    protected final HttpRequestBody requestBody;

    protected final HttpRequestHeader requestHeader;

    protected final String url;

    protected final Method method;

    protected final Protocol protocol;
    /** A monitor to record the traffic. */
    protected final TrafficStatus trafficStatus;

    protected final HttpExecutor httpExecutor;

    public BaseHttpRequest(String url, Method method, Protocol protocol) {
        this.url = url;
        this.method = method;
        this.protocol = protocol;
        this.requestBody = new HttpRequestBody();
        this.requestHeader = new HttpRequestHeader();
        this.trafficStatus = TrafficStatus.creator();
    }
    public BaseHttpRequest(String url, Method method) {
        this(url, method, Protocol.HTTP_1_1);
    }

    public BaseHttpRequest(String url) {
        this(url, Method.GET, Protocol.HTTP_1_1);
    }

    public HttpRequestBody getRequestBody() {
        return requestBody;
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
}
