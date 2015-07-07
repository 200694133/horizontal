package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpResponseBody;
import com.hanyanan.http.internal.HttpResponseHeader;
import com.hanyanan.http.internal.RedirectedResponse;
import com.hyn.job.CallbackDelivery;
import java.io.IOException;

/**
 * Created by hanyanan on 2015/6/12.
 * A proxy class for http executor.
 */
public class HttpJobLoaderProxy implements HttpLoader {
    private final HttpLoader workExecutor;
    private final HttpRequestJob asyncJob;

    public HttpJobLoaderProxy(HttpRequestJob asyncJob, HttpLoader workExecutor) {
        this.workExecutor = workExecutor;
        this.asyncJob = asyncJob;
    }

    @Override
    public void onPrepareRunning(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workExecutor.onPrepareRunning(request);
    }

    @Override
    public void onPropertyInit(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workExecutor.onPropertyInit(request);
    }

    @Override
    public void onWriteRequestHeaderFinish(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workExecutor.onWriteRequestHeaderFinish(request);
    }

    @Override
    public void onTransportUpProgress(HttpRequest request, long position, long count) throws IOException {
        if(asyncJob.isCanceled()) {
            throw new IOException("Cancel redirect!");
        }
        CallbackDelivery delivery = asyncJob.getCallbackDelivery();
        delivery.postIntermediate(asyncJob, new SimpleHttpProgress(false, asyncJob, request, position, count));
        workExecutor.onTransportUpProgress(request, position , count);
    }

    @Override
    public void onWriteRequestBodyFinish(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workExecutor.onWriteRequestBodyFinish(request);
    }

    @Override
    public int onReadResponseCode(HttpRequest request, int code) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workExecutor.onReadResponseCode(request, code);
    }

    @Override
    public HttpResponseHeader onReadResponseHeader(HttpRequest request, HttpResponseHeader responseHeader) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workExecutor.onReadResponseHeader(request, responseHeader);
    }

    @Override
    public RedirectedResponse onPrepareRedirect(HttpRequest request, RedirectedResponse redirectedResponse, int currCount) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workExecutor.onPrepareRedirect(request, redirectedResponse, currCount);
    }

    @Override
    public void onTransportDownProgress(HttpRequest request, long position, long count) throws IOException {
        if(asyncJob.isCanceled()) {
            throw new IOException("Cancel redirect!");
        }
        if(null != asyncJob.getCallback()) {
            CallbackDelivery delivery = asyncJob.getCallbackDelivery();
            delivery.postIntermediate(asyncJob, new SimpleHttpProgress(true, asyncJob, request, position, count));
        }
        workExecutor.onTransportDownProgress(request, position, count);
    }

    @Override
    public HttpResponseBody onReadRequestBodyFinish(HttpRequest request, HttpResponseBody responseBody) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workExecutor.onReadRequestBodyFinish(request, responseBody);
    }

    @Override
    public HttpResponse onAfterRunning(HttpRequest request, HttpResponse response) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workExecutor.onAfterRunning(request, response);
    }

    @Override
    public HttpResponse performRequest(HttpRequest request) throws Throwable {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workExecutor.performRequest(request);
    }
}