package com.hanyanan.http.internal;


import static hyn.com.lib.Preconditions.checkNotNull;
import static hyn.com.lib.Preconditions.checkArgument;

import android.support.annotation.Nullable;
import com.hanyanan.http.Method;
import com.hanyanan.http.Protocol;
import java.util.HashMap;
import java.util.Map;
import hyn.com.lib.Preconditions;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/13.
 * The main http request.
 */
public class HttpRequest {
    /** http request body, it it's a  */
    private final HttpRequestBody requestBody;

    private final HttpRequestHeader requestHeader;

    private final String url;

    //The redirect url
    private String forwardUrl;

    private final Method method;

    private final Protocol protocol;
    /** A monitor to record the traffic. */
    private final TrafficStatus trafficStatus;
    /** A monitor to record the http time status */
    private final TimeStatus timeStatus;
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
        this.timeStatus = new TimeStatus();
    }

    public HttpRequest(String url, Method method) {
        this(url, method, Protocol.HTTP_1_1);
    }

    public HttpRequest(String url) {
        this(url, Method.GET, Protocol.HTTP_1_1);
    }

    public HttpRequest setForwardUrl(String url) {
        this.forwardUrl = url;
        return this;
    }

    public String getForwardUrl(){
        return forwardUrl;
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

    public final void setTag(Object tag) {
        this.tag = tag;
    }

    public final Object getTag(){
        return tag;
    }

    public HttpRequest addBodyEntity(BinaryResource resource){


        return this;
    }

    public HttpRequest setCookie(String cookie){
        getRequestHeader().setRequestCookie(cookie);
        return this;
    }

    public HttpRequest range(long start, long count){
        getRequestHeader().setRequestRange(start, count);
        return this;
    }

    public HttpRequest param(Map<String, Object> params){
        //TODO encode
        if(null != params) {
            this.params.putAll(params);
        }
        return this;
    }

    public HttpRequest setRequestSupportCache(final boolean supportCache){
        //TODO
        return this;
    }

    public HttpRequest setRequestCharset(String charset) {
        //TODO
        return this;
    }

    public HttpRequest setReferer(String referer) {
        //TODO
        return this;
    }

    public HttpRequest setAuthorization(String name, String passwd) {
        //TODO
        return this;
    }

    public HttpRequest setETag(String eTag){
        //TODO
        return this;
    }

    public HttpRequest setLastModifiedTime(long time) {
        //TODO
        return this;
    }

    public HttpRequest setHeadProperty(String key, String value){
        checkNotNull(key);
        checkNotNull(value);
        getRequestHeader().setHeadProperty(key,value);
        return this;
    }
//
//
//    public static class Builder {
//        String url;
//        public Builder(String url) {
//
//        }
//        public HttpRequest post(BinaryResource body){
//
//        }
//    }
}
