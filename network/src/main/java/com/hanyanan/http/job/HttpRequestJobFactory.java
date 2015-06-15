package com.hanyanan.http.job;

import com.hanyanan.http.DefaultHttpRetryPolicy;
import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.PriorityPolicy;

/**
 * Created by hanyanan on 2015/6/14.
 */
public class HttpRequestJobFactory {
    private HttpRequestJobFactory(){
        //Can not
    }




    public static AsyncJob<HttpRequest, TransportProgress, HttpResponse> createRequestJob(HttpRequest request, JobCallback<TransportProgress, HttpResponse> callback){
        HttpRequestJob job = new HttpRequestJob(request, callback);
        return job;
    }

    public static AsyncJob<HttpRequest, TransportProgress, byte[]> createByteArrayRequestJob(HttpRequest request, JobCallback<TransportProgress, byte[]> callback){
        HttpByteArrayRequestJob job = new HttpByteArrayRequestJob(request, callback);
        return job;
    }

    public static AsyncJob<HttpRequest, TransportProgress, String> createStringRequestJob(HttpRequest request, JobCallback<TransportProgress, String> callback){
        HttpStringRequestJob job = new HttpStringRequestJob(request, callback);
        return job;
    }

    private static class HttpByteArrayRequestJob extends AsyncJob<HttpRequest, TransportProgress, byte[]> {
        public HttpByteArrayRequestJob(HttpRequest request, JobCallback<TransportProgress, byte[]> callback) {
            super(request, callback, CallbackDelivery.DEFAULT_CALLBACK_DELIVERY, new DefaultHttpRetryPolicy(),
                    PriorityPolicy.DEFAULT_PRIORITY_POLICY, new HttpFingerPrint(request), new HttpByteArrayRequestJobExecutor());
        }
    }
}
