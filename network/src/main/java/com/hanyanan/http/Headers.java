package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/5/10.
 * https://greenbytes.de/tech/webdav/draft-ietf-httpbis-p5-range-latest.html
 * http://stackoverflow.com/questions/18315787/http-1-1-response-to-multiple-range
 *
 */
public enum Headers {
    /**
     * Some demo
     * HTTP/1.1 206 Partial Content
     * Date: Tue, 14 Nov 1995 06:25:24 GMT
     * Last-Modified: Tue, 14 July 04:58:08 GMT
     * Content-Length: 2331785
     * Content-Type: multipart/byteranges; boundary=THIS_STRING_SEPARATES
     * <p/>
     * --THIS_STRING_SEPARATES
     * Content-Type: video/example
     * Content-Range: exampleunit 1.2-4.3/25
     * <p/>
     * ...the first range...
     * --THIS_STRING_SEPARATES
     * Content-Type: video/example
     * Content-Range: exampleunit 11.2-14.3/25
     * <p/>
     * ...the second range
     * --THIS_STRING_SEPARATES--
     */


    /* *
     *   请求头字段	                         说明	                       响应头字段
     *      Accept	               告知服务器发送何种媒体类型	             Content-Type
     * Accept-Language	             告知服务器发送何种语言	                 Content-Language
     * Accept-Charset	            告知服务器发送何种字符集	             Content-Type
     * Accept-Encoding	            告知服务器采用何种压缩方式	             Content-Encoding
     * */


    /**
     * 在HTTP中，与字符集和字符编码相关的消息头是Accept-Charset/Content-Type，另外主区区分Accept-Charset/Accept-Encoding/Accept-Language/Content-Type/Content-Encoding/Content-Language：
     * <p/>
     * Accept-Charset：浏览器申明自己接收的字符集，这就是本文前面介绍的各种字符集和字符编码，如gb2312，utf-8（通常我们说Charset包括了相应的字符编码方案）；
     * <p/>
     * Accept-Encoding：浏览器申明自己接收的编码方法，通常指定压缩方法，是否支持压缩，支持什么压缩方法（gzip，deflate），（注意：这不是只字符编码）；
     * <p/>
     * Accept-Language：浏览器申明自己接收的语言。语言跟字符集的区别：中文是语言，中文有多种字符集，比如big5，gb2312，gbk等等；
     * <p/>
     * Content-Type：WEB服务器告诉浏览器自己响应的对象的类型和字符集。例如：Content-Type: text/html; charset='gb2312'
     * <p/>
     * Content-Encoding：WEB服务器表明自己使用了什么压缩方法（gzip，deflate）压缩响应中的对象。例如：Content-Encoding：gzip
     * <p/>
     * Content-Language：WEB服务器告诉浏览器自己响应的对象的语言。
     */

    //common partials
    Connection("Connection"),//Connection: close
    Cache_Control("Cache-Control"),//Cache-Control: no-cache
    Content_Type("Content-Type"),//Content-Type: application/x-www-form-urlencoded//Content-Type: text/html; charset=utf-8
    Pragma("Pragma"),//Pragma: no-cache
    Via("Via"),//Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
    Warn("Warn"),//Warn: 199 Miscellaneous warning


    //client request partials
    Accept("Accept"),//Accept: text/plain, text/html
    Accept_Charset("Accept-Charset"),//Accept-Charset: iso-8859-5, unicode-1-1;q=0.8
    Accept_Encoding("Accept-Encoding"),//Accept-Encoding: compress, gzip
    Accept_Language("Accept-Language"),//Accept-Language: en,zh
    Accept_Ranges("Accept-Ranges"),//Accept-Ranges: bytes
    Range("Range"),//Range: bytes=500-999
    Authorization("Authorization"),//Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    Cookie("Cookie"),//Cookie: $Version=1; Skin=new;
    Date("Date"),//Date: Tue, 15 Nov 2010 08:12:31 GMT
    From("From"),//From: user@email.com
    Host("Host"),//Host: www.zcmhi.com
    If_Match("If-Match"),//If-Match: ��737060cd8c284d8af7ad3082f209582d��
    If_Modified_Since("If-Modified-Since"),//If-Modified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    If_None_Match("If-None-Match"),//If-None-Match: ��737060cd8c284d8af7ad3082f209582d��
    If_Range("If-Range"),//If-Range: ��737060cd8c284d8af7ad3082f209582d��
    If_Unmodified_Since("If-Unmodified-Since"),//If-Unmodified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    Max_Forwards("Max-Forwards"),//Max-Forwards: 10
    Proxy_Authorization("Proxy-Authorization"),//Proxy-Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    Referer("Referer"),//Referer: http://www.zcmhi.com/archives/71.html
    User_Agent("User-Agent"),//User-Agent: Mozilla/5.0 (Linux; X11)
    Upgrade("Upgrade"),//Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11

    //server response partials
    Content_Encoding("Content-Encoding"),//可以参考的值为：gzip,compress,deflate和identity。
    Age("Age"),//Age: 12
    Allow("Allow"),//Allow: GET, HEAD
    //http://stackoverflow.com/questions/18315787/http-1-1-response-to-multiple-range
    Content_Length("Content-Length"),//Content-Length: 348, transfer-length of message body.
    Content_Location("Content-Location"),//Content-Location: /index.htm
    Location("Location"),//Location: http://www.zcmhi.com/archives/94.html
    Content_MD5("Content-MD5"),//Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
    Content_Range("Content-Range"),//Content-Range: bytes 21010-47021/47022
    ETag("ETag"),//ETag: ��737060cd8c284d8af7ad3082f209582d��
    Expires("Expires"),//Expires: Thu, 01 Dec 2010 16:00:00 GMT
    Last_Modified("Last-Modified"),//Last-Modified: Tue, 15 Nov 2010 12:45:26 GMT
    Refresh("Refresh"),//Refresh: 5; url=http://www.zcmhi.com/archives/94.html
    Retry_After("Retry-After"),//Retry-After: 120
    Server("Server"),//Server: Apache/1.3.27 (Unix) (Red-Hat/Linux)
    Set_Cookie("Set-Cookie"),//Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
    Trailer("Trailer"),//Trailer: Max-Forwards
    TRANSFER_ENCODING("Transfer-Encoding"),//Transfer-Encoding:chunked, 有效的值为：Trunked和Identity.
    Vary("Vary"),//Vary: *
    WWW_AUTHENTICATE("WWW-Authenticate");//WWW-Authenticate: Basic


    private final String header;

    private Headers(String header) {
        this.header = header.toLowerCase();
    }

    @Override
    public String toString() {
        return header;
    }

    public String value(){
        return header;
    }
}
