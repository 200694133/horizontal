package com.hanyanan.http.internal;

import android.support.annotation.Nullable;

import com.hanyanan.http.Method;
import com.hanyanan.http.Protocol;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2015/5/13.
 * The main http request.
 */
public class HttpRequest {
    /** http request body, it it's a  */
    @Nullable private final HttpRequestBody requestBody;

    private final HttpRequestHeader requestHeader;

    private final String url;

    private final Method method;

    private final Protocol protocol;
    /** A monitor to record the traffic. */
    private final TrafficStatus trafficStatus;
    /** A monitor to record the http time status */
    private final TimeStatus timeStatus;
    /** The request executor */
    private final HttpExecutor httpExecutor;
    /** The tag user for caller identify the request. */
    private Object tag;
    /**  */
    private final Map<String, Object> params = new HashMap<String, Object>();

    public HttpRequest(String url, Method method, Protocol protocol) {
        this.url = url;
        this.method = method;
        this.protocol = protocol;
        this.requestBody = new HttpRequestBody();
        this.requestHeader = new HttpRequestHeader();
        this.trafficStatus = TrafficStatus.creator();
        this.httpExecutor = HttpExecutor.sHttpExecutor;
        this.timeStatus = new TimeStatus();
    }

    public HttpRequest(String url, Method method) {
        this(url, method, Protocol.HTTP_1_1);
    }

    public HttpRequest(String url) {
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

    public HttpRequest addCookie(String cookie){
        //TODO
        return this;
    }

    public HttpRequest range(long start, long count){
        //TODO
        return this;
    }

    public HttpRequest putParam(String name, Object value) {
        //TODO
        return this;
    }

    public HttpRequest putParam(Map<String, Object> params) {
        //TODO
        return this;
    }
}
