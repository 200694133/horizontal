package com.hanyanan.http.cache;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.job.HttpFingerPrint;

import hyn.com.lib.DelayValueReference;

/**
 * Created by hanyanan on 2015/7/9.
 */
public class HttpResponseCache {

    public DelayValueReference<HttpResponse> read(HttpRequest request) {


        return null;
    }



    // Save current resonse to
    public DelayValueReference<HttpResponse> save(HttpRequest request, HttpResponse httpResponse){
        /*
        * Save response header to database.
        * */





        /*
        * Save response body to database or disk.
        * */




        return null;
    }
}
