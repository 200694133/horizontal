package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/5/13.
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
}
