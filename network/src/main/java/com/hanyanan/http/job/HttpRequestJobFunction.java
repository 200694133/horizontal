package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpLoaderFactory;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.JobFunction;

/**
 * Created by hanyanan on 2015/7/4.
 */
public class HttpRequestJobFunction implements JobFunction<HttpRequest, HttpResponse> {
    public static final HttpRequestJobFunction DEFAULT_EXECUTOR = new HttpRequestJobFunction();
    @Override
    public HttpResponse call(AsyncJob asyncJob, HttpRequest request) throws Throwable {
        HttpLoader loader = HttpLoaderFactory.createHttpExecutor(request);
        HttpJobLoaderProxy loaderProxy = new HttpJobLoaderProxy(asyncJob, loader);
        return loaderProxy.performRequest(request);
    }
}
