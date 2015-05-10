package com.hanyanan.http.internal;

/**
 * Created by hanyanan on 2015/5/10.
 */
public enum Headers {
    //common partials
    Connection("Connection"),//Connection: close
    Cache_Control("Cache-Control"),//Cache-Control: no-cache
    Content_Type("Content-Type"),//Content-Type: application/x-www-form-urlencoded//Content-Type: text/html; charset=utf-8
    Pragma("Pragma"),//Pragma: no-cache
    Via("Via"),//Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
    Warn("Warn"),//Warn: 199 Miscellaneous warning

    //client request partials
    Accept("Accept"),//Accept: text/plain, text/html
    Accept_Charset("Accept-Charset"),//Accept-Charset: iso-8859-5
    Accept_Encoding("Accept-Encoding"),//Accept-Encoding: compress, gzip
    Accept_Language("Accept-Language"),//Accept-Language: en,zh
    Accept_Ranges("Accept-Ranges"),//Accept-Ranges: bytes
    Range("Range"),//Range: bytes=500-999
    Authorization("Authorization"),//Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    Cookie("Cookie"),//Cookie: $Version=1; Skin=new;
    Date("Date"),//Date: Tue, 15 Nov 2010 08:12:31 GMT
    From("From"),//From: user@email.com
    Host("Host"),//Host: www.zcmhi.com
    If_Match("If-Match"),//If-Match: ¡°737060cd8c284d8af7ad3082f209582d¡±
    If_Modified_Since("If-Modified-Since"),//If-Modified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    If_None_Match("If-None-Match"),//If-None-Match: ¡°737060cd8c284d8af7ad3082f209582d¡±
    If_Range("If-Range"),//If-Range: ¡°737060cd8c284d8af7ad3082f209582d¡±
    If_Unmodified_Since("If-Unmodified-Since"),//If-Unmodified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    Max_Forwards("Max-Forwards"),//Max-Forwards: 10
    Proxy_Authorization("Proxy-Authorization"),//Proxy-Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    Referer("Referer"),//Referer: http://www.zcmhi.com/archives/71.html
    User_Agent("User-Agent"),//User-Agent: Mozilla/5.0 (Linux; X11)
    Upgrade("Upgrade"),//Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11

    //server response partials
    Age("Age"),//Age: 12
    Allow("Allow"),//Allow: GET, HEAD
    Content_Length("Content-Length"),//Content-Length: 348
    Content_Location("Content-Location"),//Content-Location: /index.htm
    Location("Location"),//Location: http://www.zcmhi.com/archives/94.html
    Content_MD5("Content-MD5"),//Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
    Content_Range("Content-Range"),//Content-Range: bytes 21010-47021/47022
    ETag("ETag"),//ETag: ¡°737060cd8c284d8af7ad3082f209582d¡±
    Expires("Expires"),//Expires: Thu, 01 Dec 2010 16:00:00 GMT
    Last_Modified("Last-Modified"),//Last-Modified: Tue, 15 Nov 2010 12:45:26 GMT
    Refresh("Refresh"),//Refresh: 5; url=http://www.zcmhi.com/archives/94.html
    Retry_After("Retry-After"),//Retry-After: 120
    Server("Server"),//Server: Apache/1.3.27 (Unix) (Red-Hat/Linux)
    Set_Cookie("Set-Cookie"),//Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
    Trailer("Trailer"),//Trailer: Max-Forwards
    Transfer_Encoding("Transfer-Encoding"),//Transfer-Encoding:chunked
    Vary("Vary"),//Vary: *
    WWW_Authenticate("WWW-Authenticate");//WWW-Authenticate: Basic


    private String header;
    private Headers(String header) {
        this.header = header;
    }

    @Override public String toString(){
        return header;
    }
}
