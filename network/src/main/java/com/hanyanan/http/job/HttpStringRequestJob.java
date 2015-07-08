package com.hanyanan.http.job;

import com.hanyanan.http.DefaultHttpRetryPolicy;
import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpLoaderFactory;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.PriorityPolicy;

/**
 * Created by hanyanan on 2015/6/14.
 */
public class HttpStringRequestJob extends AsyncJob<HttpRequest, TransportProgress, String> {
    public HttpStringRequestJob(HttpRequest request, JobCallback<TransportProgress, String> callback) {
        super(request, callback);
    }

    public HttpStringRequestJob(HttpRequest request, JobCallback<TransportProgress, String> callback,
                          CallbackDelivery callbackDelivery) {
        super(request, callback, callbackDelivery);
    }

    @Override
    public String performRequest() throws Throwable {
        HttpLoader loader = new HttpJobLoaderProxy(this, HttpLoaderFactory.createHttpExecutor(getParam()));
        HttpResponse response = loader.performRequest(getParam());
        return null;
    }

}
