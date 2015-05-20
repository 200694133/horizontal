package com.hanyanan.http;

import com.hanyanan.http.internal.HttpRequest;

import java.io.IOException;
import java.io.InputStream;

import hyn.com.lib.android.parser.ObjectParser;

/**
 * Created by hanyanan on 2015/5/13.
 * Please note that this service just support utf-8.
 */
public class HttpService {
    private static HttpService sHttpService;
    public static synchronized HttpService getInstance(){
        if(null == sHttpService) {
            sHttpService = new HttpService();
        }
        return sHttpService;
    }

    private HttpService(){}

    public void setDNSBooster(DNSBooster dnsBooster) {
        //TODO
    }

    public int getTimeout(){
        return HttpUtil.DEFAULT_TIMEOUT;
    }

    public String getCharset(){
        return HttpUtil.DEFAULT_CHARSET;
    }

    public String getEncoding(){
        return HttpUtil.DEFAULT_ENCODING;
    }

    public String getUserAgent(){
        return HttpUtil.DEFAULT_USER_AGENT;
    }

    public HttpResponse loadHttpRequest(HttpRequest request) {

        return null;
    }

    public String loadStringHttpRequest(HttpRequest httpRequest) {
        return null;
    }

    public byte[] loadByteArrayHttpRequest(HttpRequest httpRequest) {
        return null;
    }

    public InputStream loadResourceHttpRequest(HttpRequest httpRequest) throws IOException{

        return null;
    }

    public <T> T loadObjectHttpRequest(HttpRequest httpRequest, ObjectParser<T> parser) {

        return null;
    }
}
