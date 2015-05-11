package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpRequestBuilder {
    public HttpRequestBuilder url(String url) {

        return this;
    }

    public HttpRequest build() {
        return null;
    }

    public HttpRequestBody.Builder getBodyBuilder(){
        return null;
    }

    public HttpRequestHeader.Builder getHeaderBuilder(){
        return null;
    }

    public HttpRequestParam.Builder getParamBuilder(){
        return null;
    }

}
