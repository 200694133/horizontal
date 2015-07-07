package com.hanyanan.http.internal;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.Method;

/**
 * Created by hanyanan on 2015/7/4.
 */
public class HttpLoaderFactory {
    public static HttpLoader createHttpExecutor(HttpRequest request){
        if(request.getMethod() == Method.GET) {
            return new HttpGetLoader();
        }

        return new HttpPostLoader();
    }
}
