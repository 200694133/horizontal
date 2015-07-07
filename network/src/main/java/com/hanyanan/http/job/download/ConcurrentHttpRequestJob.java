package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.job.HttpRequestJob;
import com.hyn.job.JobCallback;
import com.hyn.job.JobExecutor;

/**
 * Created by n550 on 2015/7/7.
 */
public class ConcurrentHttpRequestJob extends HttpRequestJob implements JobExecutor<HttpRequest, HttpResponse>{
    public ConcurrentHttpRequestJob(HttpRequest request, JobCallback<TransportProgress, HttpResponse> callback) {
        super(request, callback);
    }

    public ConcurrentHttpRequestJob(HttpRequest request, JobExecutor jobExecutor, JobCallback<TransportProgress, HttpResponse> callback) {
        super(request, jobExecutor, callback);
    }

    @Override
    public HttpResponse performRequest(HttpRequest asyncJob) throws Throwable {
        return null;
    }
}
