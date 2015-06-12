package com.hyn.job;

import com.hyn.job.CallbackDelivery;
import com.hyn.job.AsyncJob;

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

    public DefaultCallbackDelivery() {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor(){
            @Override public void execute(Runnable command) {
                command.run();
            }
        };
    }

    @Override
    public <R> void postSuccess(AsyncJob<?, ?, R> asyncJob, R response) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-response");
        mResponsePoster.execute(new ResponseDeliveryRunnable<R>(asyncJob, response));
    }

    @Override
    public void postCanceled(AsyncJob asyncJob) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-Canceled");
        mResponsePoster.execute(new ResponseDeliveryRunnable(asyncJob, null));
    }

    @Override
    public void postFailed(AsyncJob asyncJob, String msg, Throwable throwable) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-error");
        mResponsePoster.execute(new FailedMessageDeliveryRunnable(asyncJob, msg, throwable));
    }

    @Override
    public <T> void postIntermediate(AsyncJob<?, T, ?> asyncJob, T intermediate) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-intermediate");
        mResponsePoster.execute(new IntermediateDeliveryRunnable<T>(asyncJob, intermediate));
    }

    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     * Delivery success and cancel status.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable<T> implements Runnable {
        private final AsyncJob asyncJob;
        private final T response;

        public ResponseDeliveryRunnable(AsyncJob asyncJob, T response) {
            this.asyncJob = asyncJob;
            this.response = response;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("canceled-at-delivery");
                asyncJob.deliverCanceled();
                return ;
            }

            asyncJob.addMarker("finished-at-success");
            asyncJob.deliverResponse(response);
        }
    }

    /**
     * Delivery under failed status.
     */
    private class FailedMessageDeliveryRunnable implements Runnable {
        private final AsyncJob asyncJob;
        private final String errorMsg;
        private final Throwable throwable;

        public FailedMessageDeliveryRunnable(AsyncJob asyncJob, String msg, Throwable throwable) {
            this.asyncJob = asyncJob;
            this.throwable = throwable;
            this.errorMsg = msg;
        }

        @Override
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("canceled-at-delivery");
                asyncJob.deliverCanceled();
                return ;
            }
            asyncJob.addMarker("finished-at-error");
            asyncJob.deliverError(errorMsg, throwable);
        }
    }

    /**
     * Delivery under failed status.
     */
    private class IntermediateDeliveryRunnable<I> implements Runnable {
        private final AsyncJob asyncJob;
        private final I intermediate;

        public IntermediateDeliveryRunnable(AsyncJob asyncJob, I intermediate) {
            this.asyncJob = asyncJob;
            this.intermediate = intermediate;
        }

        @Override
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("canceled-at-delivery");
                asyncJob.deliverCanceled();
                return ;
            }
            asyncJob.addMarker("intermediate-response");
            asyncJob.deliverIntermediate(intermediate);
        }
    }
}
