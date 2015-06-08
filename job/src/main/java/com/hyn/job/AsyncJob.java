package com.hyn.job;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.SimpleFingerprint;

/**
 * Created by hanyanan on 2015/5/31.
 */
public class AsyncJob<P, I, R> implements Comparable<AsyncJob> {
    private boolean disposeMark = false;
    /**
     * Request callback.
     */
    @Nullable
    protected final JobCallback<I, R> callback;
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
    private final RunningTrace runningTrace = new RunningTrace();
    /**
     * The fingerprint of current request.
     */
    @NotNull
    private final Fingerprint fingerprint;
    /**
     * request status.
     */
    @NotNull
    private JobStatus jobStatus = JobStatus.IDLE;
    /**
     * Current request executor. {@see RequestExecutor#performRequest}.
     */
    @NotNull
    private final JobExecutor<R> jobExecutor;

    /**
     * An opaque token tagging this request; used for bulk cancellation.
     */
    protected Object tag;

    /** The request queue has binded. */
    private JobQueue jobQueue;

    public AsyncJob(P param, JobCallback callback, CallbackDelivery callbackDelivery,
                    RetryPolicy retryPolicy, PriorityPolicy priorityPolicy,
                    Fingerprint fingerprint, JobExecutor<R> jobExecutor) {
        this.param = param;
        this.callback = callback;
        this.callbackDelivery = callbackDelivery;
        this.retryPolicy = retryPolicy;
        this.priorityPolicy = priorityPolicy;
        this.fingerprint = fingerprint;
        this.jobExecutor = jobExecutor;
    }

    public void setPriorityPolicy(PriorityPolicy priorityPolicy) {
        this.priorityPolicy = priorityPolicy;
    }

    public JobCallback<I, R> getCallback() {
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

    public final RunningTrace getRunningTrace() {
        return runningTrace;
    }

    public Fingerprint getFingerprint() {
        return fingerprint;
    }

    public JobExecutor<R> getJobExecutor() {
        return jobExecutor;
    }

    public JobStatus getJobStatus() {
        synchronized (this) {
            return jobStatus;
        }
    }

    public void setJobStatus(JobStatus status) {
        synchronized (this) {
            jobStatus = status;
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
    public int compareTo(AsyncJob o) {
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
        if (AsyncJob.class.isInstance(obj)) {
            AsyncJob other = (AsyncJob) obj;
            return this.fingerprint.equals(other.fingerprint);
        }
        return false;
    }

    void setJobQueue(JobQueue jobQueue){
        this.jobQueue = jobQueue;
    }

    public static class Builder<P, I, R> {
        private P param;
        private JobCallback callback;
        private CallbackDelivery callbackDelivery;
        private RetryPolicy retryPolicy;
        private PriorityPolicy priorityPolicy;
        private RunningTrace runningTrace;
        private Fingerprint fingerprint = new SimpleFingerprint();
        private JobExecutor<R> jobExecutor;

        public Builder<P, I, R> setCallback(JobCallback callback) {
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

        public Builder<P, I, R> setRunningTrace(RunningTrace runningTrace) {
            this.runningTrace = runningTrace;
            return this;
        }

        public Builder<P, I, R> setFingerprint(Fingerprint fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public Builder<P, I, R> setJobExecutor(JobExecutor<R> jobExecutor) {
            this.jobExecutor = jobExecutor;
            return this;
        }

        public AsyncJob<P, I, R> build() {
            return new AsyncJob(param, callback, callbackDelivery, retryPolicy, priorityPolicy,
                    fingerprint, jobExecutor);
        }
    }
}
