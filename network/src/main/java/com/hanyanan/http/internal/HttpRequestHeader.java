package com.hanyanan.http.internal;

/**
 * Created by hanyanan on 2015/5/9.
 */
public class HttpRequestHeader extends HttpHeader{


    public HttpRequestHeader(HttpHeader header) {
        super(header);
    }

    public HttpRequestHeader() {
        this(null);
    }

    public HttpHeader setRequestCookie(String cookie) {
        //TODO
        return this;
    }

    public String getResponseCookie(){
        //TODO
        return  null;
    }

    public HttpHeader setRequestRange(long start, long count) {
        //TODO
        return this;
    }

    public Range getResponseRange(){
        return null;
    }

    public HttpHeader setRequestSupportCache(final boolean supportCache){
        //TODO
        return this;
    }

    public boolean isResponseSupportCache() {
        return false;
    }

    public HttpHeader setRequestCharset(String charset) {
        //TODO
        return this;
    }

    public String getResponseCharset() {
        return "utf-8";
    }

    public HttpHeader setReferer(String referer) {
        //TODO
        return this;
    }

    //Content-Type：WEB服务器告诉浏览器自己响应的对象的类型和字符集。例如：Content-Type: text/html; charset='gb2312'
    public String getResponseContentType() {
        return null;
    }

    public HttpHeader setAuthorization(String name, String passwd) {
        //TODO
        return this;
    }

    public HttpHeader setETag(String eTag){
        //TODO
        return this;
    }

}
