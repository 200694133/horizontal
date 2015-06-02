package com.hyn.scheduler;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/5/31.
 */
public class Request<P, I, R> {
    private boolean disposeMark = false;
    /**
     * Request callback.
     */
    protected final RequestCallback<I, R> callback;
    /**
     * Request param.
     */
    protected final P param;
    /**
     * used to delivery response.
     */
    private final CallbackDelivery callbackDelivery;
    /**
     * retry policy used to retry current request when request failed occurred.
     */
    private final RetryPolicy retryPolicy;
    /**
     * the policy priority to decide the request priority.
     */
    private final PriorityPolicy priorityPolicy;
    /**
     * Runnig status to record the running status.
     */
    private final RunningStatus runningStatus = new RunningStatus();
    /**
     * The fingerprint of current request.
     */
    private final Fingerprint fingerprint;
    /**
     * request status.
     */
    private RequestStatus requestStatus = RequestStatus.IDLE;
    /**
     * Current request executor. {@see RequestExecutor#performRequest}.
     */
    private final RequestExecutor<R> requestExecutor;

    /**
     * An opaque token tagging this request; used for bulk cancellation.
     */
    protected Object tag;

    public Request(P param, RequestCallback callback, CallbackDelivery callbackDelivery,
                   RetryPolicy retryPolicy, PriorityPolicy priorityPolicy,
                   Fingerprint fingerprint, RequestExecutor<R> requestExecutor) {
        this.param = param;
        this.callback = callback;
        this.callbackDelivery = callbackDelivery;
        this.retryPolicy = retryPolicy;
        this.priorityPolicy = priorityPolicy;
        this.fingerprint = fingerprint;
        this.requestExecutor = requestExecutor;
    }

    public RequestCallback<I, R> getCallback() {
        return callback;
    }

    public P getParam() {
        return param;
    }

    public CallbackDelivery getCallbackDelivery() {
        return callbackDelivery;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public PriorityPolicy getPriorityPolicy() {
        return priorityPolicy;
    }

    public RunningStatus getRunningStatus() {
        return runningStatus;
    }

    public Fingerprint getFingerprint() {
        return fingerprint;
    }

    public RequestExecutor<R> getRequestExecutor() {
        return requestExecutor;
    }

    public RequestStatus getRequestStatus() {
        synchronized (this) {
            return requestStatus;
        }
    }

    public void setRequestStatus(RequestStatus status) {
        synchronized (this) {
            requestStatus = status;
        }
    }

    public final void cancel() {
        disposeMark = true;
    }

    public final boolean isCanceled() {
        return disposeMark;
    }

    /**
     * @return tag of current request
     */
    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public void addMarker(String msg){
        //TODO
    }

    public void markDelivered(){
        //TODO
    }

    public void finish(String finish){
        //TODO
    }

    public final void deliverCanceled(){
        //TODO
    }

    public final void deliverResponse(R response) {
        //TODO
    }

    public final void deliverError(String msg, Throwable throwable) {
        //TODO
    }

    public final void deliverIntermediate(I intermediate) {
        //TODO
    }

    public static class Builder<P, I, R> {
        protected P param;
        private RequestCallback callback;
        private CallbackDelivery callbackDelivery;
        private RetryPolicy retryPolicy;
        private PriorityPolicy priorityPolicy;
        private RunningStatus runningStatus;
        private Fingerprint fingerprint;
        private RequestExecutor<R> requestExecutor;

        public Builder<P, I, R> setCallback(RequestCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder<P, I, R> setParam(P param) {
            this.param = param;
            return this;
        }

        public Builder<P, I, R> setCallbackDelivery(CallbackDelivery callbackDelivery) {
            this.callbackDelivery = callbackDelivery;
            return this;
        }

        public Builder<P, I, R> setRetryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public Builder<P, I, R> setPriorityPolicy(PriorityPolicy priorityPolicy) {
            this.priorityPolicy = priorityPolicy;
            return this;
        }

        public Builder<P, I, R> setRunningStatus(RunningStatus runningStatus) {
            this.runningStatus = runningStatus;
            return this;
        }

        public Builder<P, I, R> setFingerprint(Fingerprint fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public Builder<P, I, R> setRequestExecutor(RequestExecutor<R> requestExecutor) {
            this.requestExecutor = requestExecutor;
            return this;
        }

        public Request<P, I, R> build() {
            return new Request(param, callback, callbackDelivery, retryPolicy, priorityPolicy,
                    fingerprint, requestExecutor);
        }
    }
}
