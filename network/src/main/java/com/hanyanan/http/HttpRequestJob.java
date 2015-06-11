package com.hanyanan.http;

import com.hanyanan.http.internal.HttpGetExecutor;
import com.hanyanan.http.internal.HttpPostExecutor;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpUrlExecutor;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobExecutor;
import com.hyn.job.PriorityPolicy;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/6/11.
 */
public class HttpRequestJob extends AsyncJob<HttpRequest, HttpProgress, HttpResponse> implements JobExecutor<HttpRequestJob, HttpResponse>
{
    public HttpRequestJob(HttpRequest param, JobCallback callback, CallbackDelivery callbackDelivery) {
        super(param, callback, callbackDelivery, new DefaultHttpRetryPolicy(), PriorityPolicy.DEFAULT_PRIORITY_POLICY,
                new HttpFingerPrint(param), null);

    }

    @Override
    public HttpResponse performRequest(HttpRequestJob asyncJob) throws Throwable {
        HttpUrlExecutor httpUrlExecutor = null;//new HttpPostExecutor();
        if(getParam().getMethod() == Method.GET) {
            httpUrlExecutor = new HttpGetExecutor();
        }else{
            httpUrlExecutor = new HttpPostExecutor();
        }
        return httpUrlExecutor.run(getParam());
    }

    private static


    private static class HttpFingerPrint implements Fingerprint {
        private final HttpRequest request;
        private HttpFingerPrint(HttpRequest request) {
            this.request = request;
        }
        @Override
        public String fingerprint() {
            return ValueUtil.md5_16(request.getUrl());
        }
    }
}
