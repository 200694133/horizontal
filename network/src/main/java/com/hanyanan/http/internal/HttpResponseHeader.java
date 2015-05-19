package com.hanyanan.http.internal;

import java.util.List;
import java.util.Map;

/**
 * Created by hanyanan on 2015/5/9.
 */
public class HttpResponseHeader extends HttpHeader{
    public HttpResponseHeader(Map<String, List<String>> headers) {
        super(headers);
    }

    public String value(String key){
        //TODO
        return null;
    }
    public String getCookie(){
        //TODO
        return  null;
    }

    public Range getRange(){
        return null;
    }

    public boolean isSupportCache() {
        return false;
    }

    public String getCharset() {
        return "utf-8";
    }

    //Content-Type：WEB服务器告诉浏览器自己响应的对象的类型和字符集。例如：Content-Type: text/html; charset='gb2312'
    public String getContentType() {
        return null;
    }

    public String getETag(){
        //TODO
        return null;
    }

    public long getExpireTime(){
        //TODO
        return -1;
    }

    public long getServerDate(){
        //TODO
        return -1;
    }

    public long getLastModified(){
        //TODO
        return -1;
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
        //TODO
        return null;
    }
}
