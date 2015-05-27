package com.hanyanan.http;


import static hyn.com.lib.Preconditions.checkNotNull;
import static hyn.com.lib.Preconditions.checkArgument;
import static hyn.com.lib.Preconditions.checkState;

import android.support.annotation.Nullable;

import com.hanyanan.http.internal.HttpPreconditions;
import com.hanyanan.http.internal.TimeStatus;
import com.hanyanan.http.internal.TrafficStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import hyn.com.lib.ValueUtil;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/13.
 * The main http request.
 */
public class HttpRequest {
    /** http request body, it it's a  */
    private HttpRequestBody requestBody;

    private HttpRequestHeader requestHeader;

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
    /** The callback bind to current request. */
    private CallBack callBack;
    /** The finger print of current request. */
    private String fingerPrint;

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

    public HttpRequest setCallBack(CallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    public CallBack getCallBack(){
        return callBack;
    }

    public HttpRequest setForwardUrl(String url) {
        this.forwardUrl = url;
        return this;
    }

    public HttpRequest setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
        return this;
    }

    public String getFingerPrint(){
        if(null == fingerPrint) {
            return ValueUtil.md5(getUrl());
        }
        return fingerPrint;
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

    final public String getUrl() {
        return url;
    }

    final public Method getMethod() {
        return method;
    }

    public String methodString(){
        return method.toString();
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
        //TODO

        return this;
    }

    public HttpRequest setHttpRequestBody(HttpRequestBody httpRequestBody){
        checkState(requestBody!=null && !requestBody.hasContent(), "The body not empty!");
        this.requestBody = httpRequestBody;
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

    public HttpRequest params(Map<String, Object> params){
        if(null != params) {
            this.params.putAll(params);
        }
        return this;
    }

    public Map<String, Object> getParams(){
        return Collections.unmodifiableMap(params);
    }


    public HttpRequest setRequestSupportCache(final boolean supportCache){
        getRequestHeader().setRequestSupportCache(supportCache);
        return this;
    }

    public HttpRequest setReferer(String referer) {
        getRequestHeader().setReferer(referer);
        return this;
    }

    public HttpRequest setAuthorization(String name, String passwd) {
        getRequestHeader().setAuthorization(name,passwd);
        return this;
    }

    public HttpRequest setETag(String eTag){
        getRequestHeader().setETag(eTag);
        return this;
    }

    public HttpRequest setLastModifiedTime(long time) {
        getRequestHeader().setLastModifiedTime(time);
        return this;
    }

    public HttpRequest setHeadProperty(String key, String value){
        checkNotNull(key);
        checkNotNull(value);
        getRequestHeader().setPriorHeadProperty(key,value);
        return this;
    }
}
