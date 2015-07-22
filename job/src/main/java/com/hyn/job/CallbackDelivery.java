package com.hyn.job;

import java.util.concurrent.Executor;

import static hyn.com.lib.Preconditions.checkNotNull;

/**
 * Created by hanyanan on 2015/6/2.
 * <p/>
 * Delivers result and all information, include response, failed message, canceling information.
 */
public class CallbackDelivery {
    public static final CallbackDelivery DEFAULT_CALLBACK_DELIVERY = new CallbackDelivery();
    /**
     * Used for posting responses, typically to the main thread.
     */
    private final Executor poster;

    /**
     * Creates a new response delivery interface.
     */
    public CallbackDelivery(Executor executor) {
        checkNotNull(executor);
        // Make an Executor that just wraps the handler.
        poster = executor;
    }

    public CallbackDelivery() {
        // Make an Executor that just wraps the handler.
        poster = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
    }


    public <R> void postSuccess(AsyncJob<?, ?, R> asyncJob, R response) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-response");
        poster.execute(new ResponseDeliveryRunnable<R>(asyncJob, response));
    }

    public <R> void postSuccess(AsyncJob<?, ?, R> asyncJob, JobCallback<?, R> callback, R response) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-response");
        poster.execute(new ResponseDeliveryRunnable<R>(asyncJob, callback, response));
    }

    public void postCanceled(AsyncJob asyncJob) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-Canceled");
        poster.execute(new ResponseDeliveryRunnable(asyncJob, null));
    }

    public void postCanceled(AsyncJob asyncJob, JobCallback callback) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-Canceled");
        poster.execute(new ResponseDeliveryRunnable(asyncJob, callback, null));
    }


    public <R> void postFailed(AsyncJob asyncJob, R response, String msg, Throwable throwable) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-error");
        poster.execute(new FailedMessageDeliveryRunnable(asyncJob, response, msg, throwable));
    }

    public <R> void postFailed(AsyncJob asyncJob, JobCallback<?, R> callback, R response, String msg, Throwable throwable) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-error");
        poster.execute(new FailedMessageDeliveryRunnable(asyncJob, callback, response, msg, throwable));
    }

    public <T> void postIntermediate(AsyncJob<?, T, ?> asyncJob, T intermediate) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-intermediate");
        poster.execute(new IntermediateDeliveryRunnable<T>(asyncJob, intermediate));
    }

    public <T> void postIntermediate(AsyncJob<?, T, ?> asyncJob, JobCallback<T, ?> callback, T intermediate) {
        checkNotNull(asyncJob);
        asyncJob.addMarker("post-intermediate");
        poster.execute(new IntermediateDeliveryRunnable<T>(asyncJob, callback, intermediate));
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
        private final JobCallback callback;

        public ResponseDeliveryRunnable(AsyncJob asyncJob, T response) {
            this.asyncJob = asyncJob;
            this.response = response;
            this.callback = null;
        }

        public ResponseDeliveryRunnable(AsyncJob asyncJob, JobCallback callback, T response) {
            this.asyncJob = asyncJob;
            this.response = response;
            this.callback = callback;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            JobCallback callback = this.callback == null ? asyncJob.getCallback() : this.callback;
            if (null == callback) {
                return;
            }
            // If this request has canceled, finish it and don't deliver.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("canceled-at-delivery");
                callback.onCanceled(asyncJob);
                return;
            }

            asyncJob.addMarker("finished-at-success");
            callback.onSuccess(asyncJob, response);
        }
    }

    /**
     * Delivery under failed status.
     */
    private class FailedMessageDeliveryRunnable<T> implements Runnable {
        private final AsyncJob asyncJob;
        private final String errorMsg;
        private final Throwable throwable;
        private final T response;
        private final JobCallback callback;

        public FailedMessageDeliveryRunnable(AsyncJob asyncJob, T response, String msg, Throwable throwable) {
            this.asyncJob = asyncJob;
            this.throwable = throwable;
            this.errorMsg = msg;
            this.response = response;
            this.callback = null;
        }

        public FailedMessageDeliveryRunnable(AsyncJob asyncJob, JobCallback callback, T response, String msg, Throwable throwable) {
            this.asyncJob = asyncJob;
            this.throwable = throwable;
            this.errorMsg = msg;
            this.response = response;
            this.callback = callback;
        }

        @Override
        public void run() {
            JobCallback callback = null == this.callback ? asyncJob.getCallback() : this.callback;
            if (null == callback) {
                return;
            }
            // If this request has canceled, finish it and don't deliver.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("canceled-at-delivery");
                callback.onCanceled(asyncJob);
                return;
            }
            asyncJob.addMarker("finished-at-error");
            callback.onFailed(asyncJob, response, errorMsg, throwable);
        }
    }

    /**
     * Delivery under failed status.
     */
    private class IntermediateDeliveryRunnable<I> implements Runnable {
        private final AsyncJob asyncJob;
        private final I intermediate;
        private final JobCallback<I, ?> callback;

        public IntermediateDeliveryRunnable(AsyncJob asyncJob, I intermediate) {
            this.asyncJob = asyncJob;
            this.intermediate = intermediate;
            this.callback = null;
        }

        public IntermediateDeliveryRunnable(AsyncJob asyncJob, JobCallback<I, ?> callback, I intermediate) {
            this.asyncJob = asyncJob;
            this.intermediate = intermediate;
            this.callback = callback;
        }

        @Override
        public void run() {
            JobCallback callback = null == this.callback ? asyncJob.getCallback() : this.callback;
            if (null == callback) {
                return;
            }
            // If this request has canceled, finish it and don't deliver.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("canceled-at-delivery");
                callback.onCanceled(asyncJob);
                return;
            }
            asyncJob.addMarker("intermediate-response");
            callback.onIntermediate(asyncJob, intermediate);
        }
    }
}
