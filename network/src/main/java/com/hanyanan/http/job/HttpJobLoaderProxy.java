package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpResponseBody;
import com.hanyanan.http.internal.HttpResponseHeader;
import com.hanyanan.http.internal.RedirectedResponse;
import com.hyn.job.AsyncJob;

import java.io.IOException;

/**
 * Created by hanyanan on 2015/6/12.
 * A proxy class for http executor.
 */
public class HttpJobLoaderProxy implements HttpLoader {
    private final HttpLoader workLoader;
    private final AsyncJob<? extends  HttpRequest, TransportProgress, ?> asyncJob;

    public HttpJobLoaderProxy(AsyncJob<? extends  HttpRequest, TransportProgress, ?> asyncJob, HttpLoader workLoader) {
        this.workLoader = workLoader;
        this.asyncJob = asyncJob;
    }

    @Override
    public void onPrepareRunning(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workLoader.onPrepareRunning(request);
    }

    @Override
    public void onPropertyInit(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workLoader.onPropertyInit(request);
    }

    @Override
    public void onWriteRequestHeaderFinish(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workLoader.onWriteRequestHeaderFinish(request);
    }

    @Override
    public void onTransportUpProgress(HttpRequest request, long position, long count) throws IOException {
        if(asyncJob.isCanceled()) {
            throw new IOException("Cancel redirect!");
        }
        asyncJob.deliverIntermediate(new SimpleHttpProgress(false, request, position, count));
        workLoader.onTransportUpProgress(request, position, count);
    }

    @Override
    public void onWriteRequestBodyFinish(HttpRequest request) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        workLoader.onWriteRequestBodyFinish(request);
    }

    @Override
    public int onReadResponseCode(HttpRequest request, int code) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workLoader.onReadResponseCode(request, code);
    }

    @Override
    public HttpResponseHeader onReadResponseHeader(HttpRequest request, HttpResponseHeader responseHeader) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workLoader.onReadResponseHeader(request, responseHeader);
    }

    @Override
    public RedirectedResponse onPrepareRedirect(HttpRequest request, RedirectedResponse redirectedResponse, int currCount) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workLoader.onPrepareRedirect(request, redirectedResponse, currCount);
    }

    @Override
    public void onTransportDownProgress(HttpRequest request, long position, long count) throws IOException {
        if(asyncJob.isCanceled()) {
            throw new IOException("Cancel redirect!");
        }
        asyncJob.deliverIntermediate(new SimpleHttpProgress(true, request, position, count));
        workLoader.onTransportDownProgress(request, position, count);
    }

    @Override
    public HttpResponseBody onReadRequestBodyFinish(HttpRequest request, HttpResponseBody responseBody) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workLoader.onReadRequestBodyFinish(request, responseBody);
    }

    @Override
    public HttpResponse onAfterRunning(HttpRequest request, HttpResponse response) throws InterruptedException {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workLoader.onAfterRunning(request, response);
    }

    @Override
    public HttpResponse performRequest(HttpRequest request) throws Throwable {
        if(asyncJob.isCanceled()) {
            throw new InterruptedException("Cancel redirect!");
        }
        return workLoader.performRequest(request);
    }
}