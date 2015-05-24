package com.hanyanan.http.internal;

import com.hanyanan.http.HttpResponse;

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












        return null;
    }

    //readResponseHeaders
    //openResponseBody
//    /** Returns true if this response redirects to another resource. */
//    public boolean isRedirect() {
//        switch (code) {
//            case HTTP_PERM_REDIRECT:
//            case HTTP_TEMP_REDIRECT:
//            case HTTP_MULT_CHOICE:
//            case HTTP_MOVED_PERM:
//            case HTTP_MOVED_TEMP:
//            case HTTP_SEE_OTHER:
//                return true;
//            default:
//                return false;
//        }
//    }

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
