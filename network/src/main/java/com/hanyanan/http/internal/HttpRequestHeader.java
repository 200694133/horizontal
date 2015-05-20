package com.hanyanan.http.internal;

import com.hanyanan.http.Headers;

import java.util.Date;

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
        put(Headers.Cookie.value(), cookie);
        return this;
    }

    public HttpHeader setRequestRange(long start, long count) {
        put(Headers.Accept_Ranges.value(), "bytes");
        put(Headers.Range.value(), "bytes="+start+"-"+(start+count));
        return this;
    }

    public HttpHeader setRequestSupportCache(final boolean supportCache){
        if(!supportCache) {

        } else {

        }
        return this;
    }

    public HttpHeader setRequestCharset(String charset) {
        put(Headers.Accept_Charset.value(), charset);
        return this;
    }

    public HttpHeader setReferer(String referer) {
        put(Headers.Referer.value(), referer);
        return this;
    }

    public HttpHeader setAuthorization(String name, String passwd) {
        //TODO
        return this;
    }

    /**
     * Server response :
     * Etag    "427fe7b6442f2096dff4f92339305444"
     * Last-Modified   Fri, 04 Sep 2009 05:55:43 GMT
     * Client send request
     * If-None-Match   "427fe7b6442f2096dff4f92339305444"
     * If-Modified-Since   Fri, 04 Sep 2009 05:55:43 GMT
     * @param eTag
     * @return
     */
    public HttpHeader setETag(String eTag){
        put(Headers.If_None_Match.value(), eTag);
        return this;
    }
    //send request with head {If-Modified-Since   Fri, 04 Sep 2009 05:55:43 GMT}
    public HttpHeader setLastModifiedTime(long time) {
        Date date = new Date(time);
        put(Headers.If_Modified_Since.value(), DateUtils.formatDate(date));
        return this;
    }
}
