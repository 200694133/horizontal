package com.hanyanan.http.internal;

import com.hanyanan.http.HttpRequest;

import java.net.HttpURLConnection;
import java.net.URL;

import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;


import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * Created by hanyanan on 2015/5/22.
 */
public class HttpUrlExecutor implements HttpExecutor {

    @Override
    public HttpResponse run(HttpRequest request) throws Throwable {
        String url = getUrl(request);
        URL address_url = null;
        HttpURLConnection connection;
        HttpResponse.Builder
        try {
            address_url = new URL(url);
            connection = (HttpURLConnection) address_url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod(request.getMethod().toString());
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charsert", "UTF-8");
            connection.setRequestProperty("Cookie", "JSESSIONID=" + paramObj.getKey());
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            connection.setIfModifiedSince(0x2304320423L);

            connection.setFixedLengthStreamingMode();










        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //TODO , close connection.
        }
        return null;
    }

    //readResponseHeaders
    //openResponseBody


    /** Returns true if this response redirects to another resource. */
    public boolean isRedirect(int code){
        switch (code) {
            case HTTP_PERM_REDIRECT:
            case HTTP_TEMP_REDIRECT:
            case HTTP_MULT_CHOICE:
            case HTTP_MOVED_PERM:
            case HTTP_MOVED_TEMP:
            case HTTP_SEE_OTHER:
                return true;
            default:
                return false;
        }
    }

    protected void writeRequestBody(HttpRequest request) {

    }

    protected void writeRequestHeader(HttpRequest request) {
        //TODO
    }

    private final boolean permitsRequestBody(HttpRequest request){
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(request.getMethod());
        Preconditions.checkNotNull(request.getMethod().toString());
        String method = request.getMethod().toString();
        return HttpPreconditions.permitsRequestBody(method);
    }

    /** Return the url will be request. */
    protected String getUrl(HttpRequest request){
        String url = request.getForwardUrl();
        if(ValueUtil.isEmpty(url)){
            url = request.getUrl();
        }
        return url;
    }
}
