package com.hanyanan.http.internal;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.HttpRequestBody;
import com.hanyanan.http.HttpRequestHeader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

import hyn.com.lib.IOUtil;
import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;
import hyn.com.lib.binaryresource.StreamBinaryResource;


import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.getFollowRedirects;

/**
 * Created by hanyanan on 2015/5/22.
 */
public abstract class HttpUrlExecutor implements HttpExecutor {

    @Override
    public HttpResponse run(HttpRequest request) throws Throwable {
        String url = getUrl(request);
        URL address_url = null;
        HttpURLConnection connection = null;
        HttpResponse.Builder builder = new HttpResponse.Builder(request);
        int redirectedCount = 0;
        try {
            address_url = new URL(url);
            connection = (HttpURLConnection) address_url.openConnection();
            connection.setRequestMethod(request.methodString());
            setTimeout(connection);//set timeout for connection
            writeRequestHeader(request, connection);//set http request header
            writeRequestBody(request, connection);//send request body to server


//            connection.setDoOutput(true);
            connection.setDoInput(true);

            int statusCode = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            HttpResponseHeader responseHeader = new HttpResponseHeader(connection.getHeaderFields());
            if(isRedirect(statusCode)){
                ++redirectedCount;
                String forwordUrl = responseHeader.getForwardUrl();

            }else{

            }







            return builder.build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //TODO , close connection.
        }
        return null;
    }

    protected HttpResponse performRequest(final HttpRequest request, final HttpResponse.Builder builder) throws Throwable {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(builder);
        String url = getUrl(request);
        URL address_url = null;
        HttpURLConnection connection = null;
        try {
            address_url = new URL(url);
            connection = (HttpURLConnection) address_url.openConnection();
            connection.setRequestMethod(request.methodString());
            setTimeout(connection);//set timeout for connection
            writeRequestHeader(request, connection);//set http request header
            writeRequestBody(request, connection);//send request body to server

            connection.setDoInput(true);

            int statusCode = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            HttpResponseHeader responseHeader = readResponseHeaders(request, connection);
            if(isRedirect(statusCode)){
                int count = builder.increaseAndGetRedirectCount();
                if(count > MAX_REDIRECT_COUNT){
                    //TODO
                }
                String forwardUrl = responseHeader.getForwardUrl();
                if(ValueUtil.isEmpty(forwardUrl)) {
                    //TODO
                }
                request.setForwardUrl(forwardUrl);
                RedirectedResponse redirectedResponse = new RedirectedResponse(statusCode, msg, forwordUrl, responseHeader);
                builder.addRedirectedResponse(redirectedResponse);
                //TODO, setCookie, Others
                connection.disconnect();
                return performRequest(request, builder);
            }else{
                HttpResponseBody responseBody = readResponseBody(request, connection);
                return responseBody;
            }







            return builder.build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }
    }

    //readResponseHeaders
    //openResponseBody

    protected HttpResponseBody readResponseBody(HttpRequest httpRequest, HttpURLConnection connection)
            throws IOException{
        InputStream inputStream = connection.getInputStream();
        InputStreamWrapper inputStreamWrapper = new InputStreamWrapper(inputStream, connection);
        long contentLength = connection.getContentLengthLong();
        HttpResponseBody responseBody = new HttpResponseBody(new StreamBinaryResource(inputStreamWrapper, contentLength));
        return responseBody;
    }


    protected HttpResponseHeader readResponseHeaders(HttpRequest httpRequest, HttpURLConnection connection)
            throws IOException{
        connection.getResponseCode();
        return new HttpResponseHeader(connection.getHeaderFields());
    }



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

    protected abstract void writeRequestBody(HttpRequest request, URLConnection connection) ;
    /**
     *
     * @param request
     */
    protected void writeRequestHeader(HttpRequest request, URLConnection connection) {
        HttpRequestHeader requestHeader = request.getRequestHeader();
        if(null == requestHeader) return ;
        Map<String, String> headers = requestHeader.maps();
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        if(null == entrySet) return ;
        for(Map.Entry<String,String> entry : entrySet) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    protected void setTimeout(URLConnection connection){
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
    }


    protected final boolean supportRequestBody(HttpRequest request){
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(request.methodString());
        String method = request.methodString();
        return HttpPreconditions.permitsRequestBody(method);
    }

    protected final boolean containRequestBody(HttpRequest request){
        if(null == request) return false;

        return request.getRequestBody().hasContent();
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
