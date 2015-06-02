package com.hyn.scheduler.impl;

import com.hyn.scheduler.CallbackDelivery;
import com.hyn.scheduler.Request;

import java.util.concurrent.Executor;

import static hyn.com.lib.Preconditions.checkNotNull;

/**
 * Created by hanyanan on 2015/6/2.
 *
 * Delivers result and all information, include response, failed message, canceling information.
 */
public class DefaultCallbackDelivery implements CallbackDelivery {
    /** Used for posting responses, typically to the main thread. */
    private final Executor mResponsePoster;

    /**
     * Creates a new response delivery interface.
     */
    public DefaultCallbackDelivery(Executor executor) {
        checkNotNull(executor);
        // Make an Executor that just wraps the handler.
        mResponsePoster = executor;
    }

    @Override
    public <R> void postSuccess(Request<?, ?, R> request, R response) {
        checkNotNull(request);
        request.addMarker("post-response");
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response));
    }

    @Override
    public void postCanceled(Request request) {
        checkNotNull(request);
        request.addMarker("post-Canceled");
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, null));
    }

    @Override
    public void postFailed(Request request, String msg, Throwable throwable) {
        checkNotNull(request);
        request.addMarker("post-error");
        mResponsePoster.execute(new FailedMessageDeliveryRunnable(request, msg, throwable));
    }

    @Override
    public <T> void postIntermediate(Request<?, T, ?> request, T intermediate) {

    }

    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     * Delivery success and cancel status.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable<T> implements Runnable {
        private final Request request;
        private final T response;

        public ResponseDeliveryRunnable(Request request, T response) {
            this.request = request;
            this.response = response;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (request.isCanceled()) {
                request.finish("canceled-at-delivery");
                request.deliverCanceled();
                return ;
            }

            request.finish("finished-at-success");
            request.deliverResponse(response);
        }
    }

    /**
     * Delivery under failed status.
     */
    private class FailedMessageDeliveryRunnable implements Runnable {
        private final Request request;
        private final String errorMsg;
        private final Throwable throwable;

        public FailedMessageDeliveryRunnable(Request request, String msg, Throwable throwable) {
            this.request = request;
            this.throwable = throwable;
            this.errorMsg = msg;
        }

        @Override
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (request.isCanceled()) {
                request.finish("canceled-at-delivery");
                request.deliverCanceled();
                return ;
            }
            request.finish("finished-at-error");
            request.deliverError(errorMsg, throwable);
        }
    }

    /**
     * Delivery under failed status.
     */
    private class IntermediateDeliveryRunnable<I> implements Runnable {
        private final Request request;
        private final I intermediate;

        public IntermediateDeliveryRunnable(Request request, I intermediate) {
            this.request = request;
            this.intermediate = intermediate;
        }

        @Override
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (request.isCanceled()) {
                request.finish("canceled-at-delivery");
                request.deliverCanceled();
                return ;
            }
            request.addMarker("intermediate-response");
            request.deliverIntermediate(intermediate);
        }
    }
}
