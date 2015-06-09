package com.hanyanan.http;

import com.hanyanan.http.internal.HttpGetExecutor;
import com.hanyanan.http.internal.HttpPostExecutor;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpUrlExecutor;

import java.io.InputStream;

import hyn.com.lib.IOUtil;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.binaryresource.BinaryResource;
import hyn.com.lib.parser.ByteArrayParser;
import hyn.com.lib.parser.ObjectParser;
import hyn.com.lib.parser.StringParser;

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

    public HttpResponse loadHttpRequest(HttpRequest request) throws Throwable{
        HttpUrlExecutor httpUrlExecutor = null;//new HttpPostExecutor();
        if(request.getMethod() == Method.GET) {
            httpUrlExecutor = new HttpGetExecutor();
        }else{
            httpUrlExecutor = new HttpPostExecutor();
        }
        return httpUrlExecutor.run(request);
    }

    public TwoTuple<Integer, String> loadStringHttpRequest(HttpRequest httpRequest) throws Throwable{
        return loadObjectHttpRequest(httpRequest, new StringParser());
    }

    public TwoTuple<Integer, byte[]> loadByteArrayHttpRequest(HttpRequest httpRequest) throws Throwable {
        return loadObjectHttpRequest(httpRequest, new ByteArrayParser());
    }

    public TwoTuple<Integer, InputStream> loadResourceHttpRequest(HttpRequest httpRequest) throws Throwable{
        try {
            HttpResponse response = loadHttpRequest(httpRequest);
            if(!response.isSuccessful()){
                System.out.println(response.toString());
                return new TwoTuple<>(response.getCode(), null);
            }
            BinaryResource resource = response.body().getResource();
            return new TwoTuple<>(response.getCode(), resource.openStream());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public <T> TwoTuple<Integer, T> loadObjectHttpRequest(HttpRequest httpRequest, ObjectParser<T> parser) throws Throwable {
        try {
            HttpResponse response = loadHttpRequest(httpRequest);
            if(!response.isSuccessful()){
                System.out.println(response.toString());
                return new TwoTuple<>(response.getCode(), null);
            }
            BinaryResource resource = response.body().getResource();
            T res = parser.transferToObject(resource.openStream(), false);
            IOUtil.closeQuietly(resource.openStream());
            return new TwoTuple<>(response.getCode(), res);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
}
