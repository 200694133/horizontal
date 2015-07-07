package com.hanyanan.http.internal;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.HttpUtil;

import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/5/27.
 */
public class HttpGetLoader extends HttpUrlLoader {
    /**
     * Return the url will be request.
     */
    @Override
    protected String getUrl(HttpRequest request) {
        String url = request.getForwardUrl();
        if (ValueUtil.isEmpty(url)) {
            url = request.getUrl();
        }

        url = HttpUtil.generateUrl(url, request.getParams());
        return url;
    }
}
