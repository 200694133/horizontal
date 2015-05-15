package com.hanyanan.http.internal;

import com.hanyanan.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import hyn.com.lib.Asyncable;

/**
 * Created by hanyanan on 2015/5/13.
 */
public class HttpExecutor implements Asyncable<HttpRequest, HttpResponse> {
    public static final HttpExecutor sHttpExecutor = new HttpExecutor();

    private HttpExecutor(){}

    @Override
    public HttpResponse run(HttpRequest request) {
        String url = request.getUrl();
        URL address_url = null;
        HttpURLConnection connection;
        try {
            address_url = new URL(url);
            connection = (HttpURLConnection) address_url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charsert", "UTF-8");
            connection.setRequestProperty("Cookie", "JSESSIONID=" + paramObj.getKey());
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            connection.setIfModifiedSince(0x2304320423L);












        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //TODO , close connection.
        }


        return null;
    }


    protected void sendBody(HttpRequest request, HttpURLConnection connection){
        if(!HttpPreconditions.requiresRequestBody(request.getMethod().toString())) return ;

    }
}
