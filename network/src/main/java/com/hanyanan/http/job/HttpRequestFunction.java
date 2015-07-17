package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpLoaderFactory;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.JobFunction;

/**
 * Created by hanyanan on 2015/7/17.
 */
public class HttpRequestFunction implements JobFunction<HttpRequest, HttpResponse> {
    @Override
    public HttpResponse call(AsyncJob asyncJob, HttpRequest httpRequest) throws Throwable {
        HttpLoader loader = new HttpJobLoaderProxy(asyncJob, HttpLoaderFactory.createHttpExecutor(httpRequest));
        return loader.performRequest(httpRequest);
    }
}
