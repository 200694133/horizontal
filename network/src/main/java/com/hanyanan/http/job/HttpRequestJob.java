package com.hanyanan.http.job;

import com.hanyanan.http.DefaultHttpRetryPolicy;
import com.hanyanan.http.HttpProgress;
import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpGetExecutor;
import com.hanyanan.http.internal.HttpPostExecutor;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpUrlExecutor;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.DefaultCallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobExecutor;
import com.hyn.job.PriorityPolicy;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/6/11.
 */
public class HttpRequestJob extends AsyncJob<HttpRequest, HttpProgress, HttpResponse> {
    public HttpRequestJob(HttpRequest request, JobCallback callback) {
        super(request, callback, CallbackDelivery.DEFAULT_CALLBACK_DELIVERY, new DefaultHttpRetryPolicy(),
                PriorityPolicy.DEFAULT_PRIORITY_POLICY, new HttpFingerPrint(request), new HttpJobExecutor());
    }
}
