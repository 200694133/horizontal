package com.hanyanan.http;

import com.hanyanan.http.internal.HttpResponseHeader;
import com.hanyanan.http.internal.Range;
import com.hanyanan.http.internal.RedirectedResponse;
import com.sun.deploy.net.HttpRequest;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpResponse implements Closeable{
    /** The raw http request */
    private final HttpRequest httpRequest;
    /** Http response header for current request. */
    private final HttpResponseHeader responseHeader;

    private final List<RedirectedResponse> redirectedResponse = new LinkedList<RedirectedResponse>();

    /** The real body which store the content response from server. */
    private final BinaryResource bodyResource;
    /** http resonse code */
    private final int code;
    /** http status message */
    private final String msg;

    private final Protocol protocol;

    private MimeType mimeType;

    public HttpResponse(int code, String msg, HttpRequest httpRequest, Protocol protocol, HttpResponseHeader responseHeader,
                        BinaryResource bodyResource){
        this.code = code;
        this.msg = msg;
        this.httpRequest = httpRequest;
        this.responseHeader = responseHeader;
        this.bodyResource = bodyResource;
        this.protocol = protocol;
    }

    public MimeType mimeType(){
        MimeType mimeType = getResponseHeader().
    }

    public List<RedirectedResponse> getRedirectedResponse(){
        return new LinkedList<>(redirectedResponse);
    }
    /**
     * Returns the response as a string decoded with the charset of the
     * Content-Type header. If that header is either absent or lacks a charset,
     * this will attempt to decode the response body as UTF-8.
     */
    public final String string() throws IOException {
        return new String(bytes(), charset().name());
    }

    private Charset charset() {
        MimeType contentType = contentType();
        return contentType != null ? contentType.charset(UTF_8) : UTF_8;
    }

    @Override public void close() throws IOException {
        source().close();
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


    public MimeType getMimeType(){
        return mimeType;
    }

    /**
     * Returns true if the code is in [200..300), which means the request was
     * successfully received, understood, and accepted.
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    @Override public String toString() {
        return "Response{protocol="
                + protocol
                + ", code="
                + code
                + ", message="
                + msg
                + ", url="
                + httpRequest.urlString()
                + '}';
    }

}

