package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpLoaderFactory;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.JobExecutor;

/**
 * Created by hanyanan on 2015/7/4.
 */
public class HttpJobExecutor implements JobExecutor<HttpRequestJob, HttpResponse> {
    public static final HttpJobExecutor DEFAULT_EXECUTOR = new HttpJobExecutor();
    @Override
    public HttpResponse performRequest(HttpRequestJob asyncJob) throws Throwable {
        HttpRequest request = asyncJob.getParam();
        HttpLoader loader = HttpLoaderFactory.createHttpExecutor(request);
        HttpJobLoaderProxy loaderProxy = new HttpJobLoaderProxy(asyncJob, loader);
        return loaderProxy.performRequest(request);
    }
}
