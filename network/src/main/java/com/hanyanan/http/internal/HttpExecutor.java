package com.hanyanan.http.internal;

import com.hanyanan.http.HttpRequest;

import hyn.com.lib.Asyncable;

/**
 * Created by hanyanan on 2015/5/13.
 */
public interface HttpExecutor extends Asyncable<HttpRequest, HttpResponse> {
    public static final int HTTP_TEMP_REDIRECT = 307;
    public static final int HTTP_PERM_REDIRECT = 308;
    public static final int MAX_REDIRECT_COUNT = 10;

    public void onPrepareRunning(HttpRequest request) throws InterruptedException;

    public void onPropertyInitFinish(HttpRequest request) throws InterruptedException;

    public void onWriteRequestHeaderFinish(HttpRequest request) throws InterruptedException;

    /**
     * @param request
     * @param position
     * @param count
     * @throws InterruptedException
     */
    public void onTransportUpProgress(HttpRequest request, long position, long count) throws InterruptedException;

    public void onWriteRequestBodyFinish(HttpRequest request) throws InterruptedException;

    public void onReadResponseCode(HttpRequest request, int code) throws InterruptedException;

    public HttpResponseHeader onReadResponseHeader(HttpRequest request, HttpResponseHeader responseHeader) throws InterruptedException;

    /**
     * Prepare redirect to the next url, invoke this method after finish a redirect request and before redirect to the
     * specify url. The imlments may be throw a InterruptedException to interrupted current request..
     *
     * @param request
     * @param redirectedResponse
     * @throws InterruptedException to abort current request
     */
    public RedirectedResponse onPrepareRedirect(HttpRequest request, RedirectedResponse redirectedResponse, int currCount) throws InterruptedException;

    public void onTransportDownProgress(HttpRequest request, long position, long count) throws InterruptedException;

    public HttpResponseBody onReadRequestBodyFinish(HttpRequest request, HttpResponseBody responseBody) throws InterruptedException;

    public HttpResponse onAfterRunning(HttpRequest request, HttpResponse response) throws InterruptedException;
}
