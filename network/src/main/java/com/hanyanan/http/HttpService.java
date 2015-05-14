package com.hanyanan.http;

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
        return Util.DEFAULT_TIMEOUT;
    }

    public String getCharset(){
        return Util.DEFAULT_CHARSET;
    }

    public String getEncoding(){
        return Util.DEFAULT_ENCODING;
    }

    public String getUserAgent(){
        return Util.DEFAULT_USER_AGENT;
    }




}
