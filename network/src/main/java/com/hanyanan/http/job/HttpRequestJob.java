package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpLoaderFactory;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;

/**
 * Created by hanyanan on 2015/6/11.
 */
public class HttpRequestJob extends AsyncJob<HttpRequest, TransportProgress, HttpResponse> {

    public HttpRequestJob(HttpRequest request, JobCallback<TransportProgress, HttpResponse> callback) {
        super(request, callback);
    }

    public HttpRequestJob(HttpRequest request, JobCallback<TransportProgress, HttpResponse> callback,
                          CallbackDelivery callbackDelivery) {
        super(request, callback, callbackDelivery);
    }

    @Override
    public HttpResponse performRequest() throws Throwable {
        HttpLoader loader = new HttpJobLoaderProxy(this, HttpLoaderFactory.createHttpExecutor(getParam()));
        return loader.performRequest(getParam());
    }
}
