package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.Method;
import com.hanyanan.http.internal.HttpGetExecutor;
import com.hanyanan.http.internal.HttpPostExecutor;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpUrlExecutor;
import com.hanyanan.http.internal.RedirectedResponse;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.CanceledException;
import com.hyn.job.JobExecutor;

/**
 * Created by hanyanan on 2015/6/12.
 */
public class HttpJobExecutor implements JobExecutor<HttpRequestJob, HttpResponse> {
    public static final HttpJobExecutor DEFAULT_EXECUTOR = new HttpJobExecutor();
    @Override
    public HttpResponse performRequest(final HttpRequestJob asyncJob) throws Throwable {
        HttpUrlExecutor httpUrlExecutor;
        if(asyncJob.getParam().getMethod() == Method.GET) {
            httpUrlExecutor = new HttpGetExecutor() {
                @Override
                protected void onPrepareRedirect(HttpRequest request, RedirectedResponse redirectedResponse) throws InterruptedException {
                    if(asyncJob.isCanceled()) {
                        throw new InterruptedException("Cancel redirect0 ");
                    }
                    super.onPrepareRedirect(request, redirectedResponse);
                }

                protected void onTransportProgress(HttpRequest request, boolean download, long offset, long count){
                    if(asyncJob.isCanceled()){ // current request job has canceled, then dispose cancel
                        throw new CanceledException();
                    }
                    CallbackDelivery delivery = asyncJob.getCallbackDelivery();
                    delivery.postIntermediate(asyncJob, new SimpleHttpProgress(download, asyncJob, request, offset, count));
                }
            };
        }else{
            httpUrlExecutor = new HttpPostExecutor() {
                @Override
                protected void onPrepareRedirect(HttpRequest request, RedirectedResponse redirectedResponse) throws InterruptedException {
                    if(asyncJob.isCanceled()) throw new InterruptedException("Cancel redirect0 ");
                    super.onPrepareRedirect(request, redirectedResponse);
                }

                protected void onTransportProgress(HttpRequest request, boolean download, long offfset, long count){
                    CallbackDelivery delivery = asyncJob.getCallbackDelivery();
                    delivery.postIntermediate(asyncJob, new SimpleHttpProgress(download, asyncJob, request, offfset, count));
                }
            };
        }
        HttpResponse response = httpUrlExecutor.run(asyncJob.getParam());
        return response;
    }
}