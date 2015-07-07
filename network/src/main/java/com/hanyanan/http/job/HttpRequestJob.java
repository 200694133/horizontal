package com.hanyanan.http.job;

import com.hanyanan.http.DefaultHttpRetryPolicy;
import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobExecutor;
import com.hyn.job.PriorityPolicy;

/**
 * Created by hanyanan on 2015/6/11.
 */
public class HttpRequestJob extends AsyncJob<HttpRequest, TransportProgress, HttpResponse> {

    public HttpRequestJob(HttpRequest request, JobCallback<TransportProgress, HttpResponse> callback) {
        super(request, callback, CallbackDelivery.DEFAULT_CALLBACK_DELIVERY, new DefaultHttpRetryPolicy(),
                PriorityPolicy.DEFAULT_PRIORITY_POLICY, new HttpFingerPrint(request), HttpJobExecutor.DEFAULT_EXECUTOR);
    }

    public HttpRequestJob(HttpRequest request, JobExecutor jobExecutor, JobCallback<TransportProgress,
            HttpResponse> callback) {
        super(request, callback, CallbackDelivery.DEFAULT_CALLBACK_DELIVERY, new DefaultHttpRetryPolicy(),
                PriorityPolicy.DEFAULT_PRIORITY_POLICY, new HttpFingerPrint(request), jobExecutor);
    }
}
