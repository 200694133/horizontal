package com.hanyanan.http.internal;


import com.hanyanan.http.Headers;

import java.util.List;
import java.util.Map;

import static hyn.com.lib.Preconditions.checkNotNull;

/**
 * Created by hanyanan on 2015/5/9.
 */
public class HttpResponseHeader extends HttpHeader{
    public HttpResponseHeader(Map<String, List<String>> headers) {
        super(headers);
    }

    public String value(String key){
        checkNotNull(key);
        if(headers.containsKey(key)){
            Object res = headers.get(key);
            if(null != res) return res.toString();
        }
        if(priorHeaders.containsKey(key)){
            Object res =  priorHeaders.get(key);
            if(null != res) return res.toString();
        }
        return null;
    }
    public String getCookie(){
        return  value(Headers.SET_COOKIE.value());
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

    /**
     * Cache-Control：
     * 请求：
     * no-cache（不要缓存的实体，要求现在从WEB服务器去取）
     * max-age：（只接受 Age 值小于 max-age 值，并且没有过期的对象）
     * max-stale：（可以接受过去的对象，但是过期时间必须小于 max-stale 值）
     * min-fresh：（接受其新鲜生命期大于其当前 Age 跟 min-fresh 值之和的缓存对象）
     * 响应：
     * public(可以用 Cached 内容回应任何用户)
     * private（只能用缓存内容回应先前请求该内容的那个用户）
     * no-cache（可以缓存，但是只有在跟WEB服务器验证了其有效后，才能返回给客户端）
     * max-age：（本响应包含的对象的过期时间,以秒计算）
     * <p/>
     * ALL: no-store（不允许缓存）
     * Currently just support no-cache and max-age
     * Cache-Control的max-age优先级高于Expires(至少对于Apache是这样的）,即如果定义了Cache-Control: max-age，
     * 则完全不需要加上Expries，因为根本没用。
     * Cache-Control在Apache中的设置为  Header set Cache-Control "max-age: 60" , Expires是相同的功能，不过参数是个绝对的日期，
     * 不是一个相对的值 Header set Expires "Thu, 15 Apr 2010 20:00:00 GMT"
     * Cache-Control会覆盖Expires字段。
     * @return
     */
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
