package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.JobFunction;

import java.io.InputStream;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/7/17.
 */
public class HttpResponseFunction implements JobFunction<HttpResponse, InputStream> {
    @Override
    public InputStream call(AsyncJob asyncJob, HttpResponse response) throws Throwable {
        if(!response.isSuccessful()){
            System.out.println(response.toString());
            return null;
        }
        BinaryResource resource = response.body().getResource();
        InputStream stream = resource.openStream();
        return stream;
    }
}
