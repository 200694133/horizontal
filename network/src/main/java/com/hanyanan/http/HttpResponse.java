package com.hanyanan.http;

import com.hanyanan.http.internal.HttpHeader;
import com.hanyanan.http.internal.HttpResponseHeader;
import com.hanyanan.http.internal.Range;
import com.sun.deploy.net.HttpRequest;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpResponse {
    /** The raw http request */
    private final HttpRequest httpRequest;
    /** Http response header for current request. */
    private final HttpResponseHeader responseHeader;
    /** The real body which store the content response from server. */
    private final BinaryResource bodyResource;
    /** http resonse code */
    private final int code;
    /** http status message */
    private final String msg;

    public HttpResponse(int code, String msg, HttpRequest httpRequest, HttpResponseHeader responseHeader,
                        BinaryResource bodyResource){
        this.code = code;
        this.msg = msg;
        this.httpRequest = httpRequest;
        this.responseHeader = responseHeader;
        this.bodyResource = bodyResource;
    }

    public final HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public final HttpResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public final BinaryResource geBody() {
        return bodyResource;
    }

    public String headValue(String key) {
        return getResponseHeader().value(key);
    }

    public final int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public BinaryResource body(){
        return bodyResource;
    }

    public String getCookie(){
        //TODO
        return  null;
    }

    public Range getRange(){
        return getResponseHeader().getRange();
    }

    public boolean isSupportCache() {
        return getResponseHeader().isSupportCache();
    }

    public String getCharset() {
        return getResponseHeader().getCharset();
    }

    //Content-Type：WEB服务器告诉浏览器自己响应的对象的类型和字符集。例如：Content-Type: text/html; charset='gb2312'
    public String getContentType() {
        return getResponseHeader().getContentType();
    }

    public String getETag(){
        return getResponseHeader().getETag();
    }

    public long getExpireTime(){
        return getResponseHeader().getExpireTime();
    }

    public long getServerDate(){
        return getResponseHeader().getServerDate();
    }

    public long getLastModified(){
        return getResponseHeader().getLastModified();
    }

    /**
     * Return the origin server to suggest a default filename
     * The server may be provide a default file name for current resource, most of time it will be return {@code null},
     * So client cannot depende on this value.
     * </pr>
     *  The Content-Disposition identify the default file name value in http headers which come from server.
     *  Content-Disposition: attachment; filename="fname.ext". it will return the fname.ext as the default download file
     *  name.
     *  </pr>
     */
    public String getDisposition(){
        return getResponseHeader().getDisposition();
    }
}

