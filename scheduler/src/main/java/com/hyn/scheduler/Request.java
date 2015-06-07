package com.hyn.scheduler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.SimpleFingerprint;

/**
 * Created by hanyanan on 2015/5/31.
 */
public class Request<P, I, R> implements Comparable<Request> {
    private boolean disposeMark = false;
    /**
     * Request callback.
     */
    @Nullable
    protected final RequestCallback<I, R> callback;
    /**
     * Request param.
     */
    @Nullable
    protected final P param;
    /**
     * used to delivery response.
     */
    @Nullable
    private final CallbackDelivery callbackDelivery;
    /**
     * retry policy used to retry current request when request failed occurred.
     */
    @NotNull
    private final RetryPolicy retryPolicy;
    /**
     * the policy priority to decide the request priority.
     */
    @NotNull
    private PriorityPolicy priorityPolicy = new PriorityPolicy();
    /**
     * Running status to record the running status.
     */
    @NotNull
    private final RunningStatus runningStatus = new RunningStatus();
    /**
     * The fingerprint of current request.
     */
    @NotNull
    private final Fingerprint fingerprint;
    /**
     * request status.
     */
    @NotNull
    private RequestStatus requestStatus = RequestStatus.IDLE;
    /**
     * Current request executor. {@see RequestExecutor#performRequest}.
     */
    @NotNull
    private final RequestExecutor<R> requestExecutor;

    /**
     * An opaque token tagging this request; used for bulk cancellation.
     */
    protected Object tag;

    /** The request queue has binded. */
    private RequestQueue requestQueue;

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

    public void setPriorityPolicy(PriorityPolicy priorityPolicy) {
        this.priorityPolicy = priorityPolicy;
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

    public final RunningStatus getRunningStatus() {
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

    public void addMarker(String msg) {
        //TODO
    }

    public void finish() {
        //TODO
    }

    /**
     * Called when afrer delivery the response success.
     */
    public void markDelivered(){
        //TODO
    }

    final void deliverCanceled() {
        if (null != callback) {
            callback.onCanceled(this);
        }
    }

    final void deliverResponse(R response) {
        if (null != callback) {
            callback.onSuccess(this, response);
        }
    }

    final void deliverError(String msg, Throwable throwable) {
        if (null != callback) {
            callback.onFailed(this, msg, throwable);
        }
    }

    final void deliverIntermediate(I intermediate) {
        if (null != callback) {
            callback.onIntermediate(intermediate);
        }
    }

    @Override
    public int compareTo(Request o) {
        return this.priorityPolicy.compareTo(o.priorityPolicy);
    }

    @Override
    public int hashCode() {
        return fingerprint.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (Request.class.isInstance(obj)) {
            Request other = (Request) obj;
            return this.fingerprint.equals(other.fingerprint);
        }
        return false;
    }

    void setRequestQueue(RequestQueue requestQueue){
        this.requestQueue = requestQueue;
    }

    public static class Builder<P, I, R> {
        private P param;
        private RequestCallback callback;
        private CallbackDelivery callbackDelivery;
        private RetryPolicy retryPolicy;
        private PriorityPolicy priorityPolicy;
        private RunningStatus runningStatus;
        private Fingerprint fingerprint = new SimpleFingerprint();
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
