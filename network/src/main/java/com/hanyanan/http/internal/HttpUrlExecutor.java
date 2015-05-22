package com.hanyanan.http.internal;

import com.hanyanan.http.HttpResponse;

import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/5/22.
 */
public class HttpUrlExecutor implements HttpExecutor {

    @Override
    public HttpResponse run(HttpRequest request) throws Throwable {
        String url = getUrl(request);












        return null;
    }

    //readResponseHeaders
    //openResponseBody


    protected void writeRequestBody(HttpRequest request) {

    }

    protected void writeRequestHeader(HttpRequest request) {
        //TODO
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
