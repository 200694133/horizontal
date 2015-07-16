package com.hyn.job;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.SimpleFingerprint;

/**
 * Created by hanyanan on 2015/5/31.
 */
public abstract class AsyncJob<P, I, R> implements Comparable<AsyncJob>, Fingerprint {
    public static final String LOG_TAG = "AsyncJob";

    /**
     * A flag to identify if it's disposed.
     */
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
    protected final CallbackDelivery callbackDelivery;

    /**
     * retry policy used to retry current request when request failed occurred.
     */
    @NotNull
    protected final RetryPolicy retryPolicy;

    /**
     * the policy priority to decide the request priority.
     */
    @NotNull
    protected PriorityPolicy priorityPolicy = new PriorityPolicy();

    /**
     * Running status to record the running status.
     */
    @NotNull
    protected final RunningTrace runningTrace = new RunningTrace();

    /**
     * The fingerprint of current request.
     */
    @NotNull
    protected final Fingerprint fingerprint;

    /**
     * request status.
     */
    @NotNull
    protected JobStatus jobStatus = JobStatus.IDLE;

    /**
     * An opaque token tagging this request; used for bulk cancellation.
     */
    protected Object tag;

    private JobResult<R> result;


    public AsyncJob(P param, JobCallback<I, R> callback, CallbackDelivery callbackDelivery,
                    RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint) {
        this.param = param;
        this.callback = callback;
        this.callbackDelivery = callbackDelivery;
        this.retryPolicy = retryPolicy;
        this.priorityPolicy = priorityPolicy;
        this.fingerprint = fingerprint;
    }

    public AsyncJob(P param, JobCallback<I, R> callback, CallbackDelivery callbackDelivery) {
        this.param = param;
        this.callback = callback;
        this.callbackDelivery = callbackDelivery;
        this.retryPolicy = RetryPolicy.UnRetryPolicy;
        this.priorityPolicy = PriorityPolicy.DEFAULT_PRIORITY_POLICY;
        this.fingerprint = this;
    }

    public AsyncJob(P param, JobCallback<I, R> callback) {
        this.param = param;
        this.callback = callback;
        this.callbackDelivery = CallbackDelivery.DEFAULT_CALLBACK_DELIVERY;
        this.retryPolicy = RetryPolicy.UnRetryPolicy;
        this.priorityPolicy = PriorityPolicy.DEFAULT_PRIORITY_POLICY;
        this.fingerprint = this;
    }

    public AsyncJob(P param, JobCallback<I, R> callback, CallbackDelivery callbackDelivery, RetryPolicy retryPolicy) {
        this.param = param;
        this.callback = callback;
        this.callbackDelivery = callbackDelivery;
        this.retryPolicy = retryPolicy;
        this.priorityPolicy = PriorityPolicy.DEFAULT_PRIORITY_POLICY;
        this.fingerprint = this;
    }

    public AsyncJob(P param, JobCallback callback, RetryPolicy retryPolicy) {
        this.param = param;
        this.callback = callback;
        this.callbackDelivery = CallbackDelivery.DEFAULT_CALLBACK_DELIVERY;
        this.retryPolicy = retryPolicy;
        this.priorityPolicy = PriorityPolicy.DEFAULT_PRIORITY_POLICY;
        this.fingerprint = this;
    }

    /**
     * Perform current request and return the result.It's a execution unit. If the input job is a
     * <br>
     * <p/>
     * User can throw some specify Throwable to retry again or force delivery failed result.
     * <hr>
     * <ul>
     * <li>{@link UnexpectedResponseException} may be attaching a unexpected response,
     * this will trigger {@link com.hyn.job.JobCallback#onFailed(AsyncJob, Object, String, Throwable)} if
     * current Job not canceled!
     * </li>
     * <li>if user throw any exception which implements {@link UnRetryable} interface, it means current job has
     * be abandoned, notify {@link JobCallback#onFailed(AsyncJob, Object, String, Throwable)} or
     * {@link JobCallback#onCanceled(AsyncJob)} callback.
     * </li>
     * <li>
     * {@link UnRetryException} is a runtime exception, which implement {@code UnRetryable}. This is
     * default exception for interrupt current job and do not retry again, will notify
     * {@link JobCallback#onFailed(AsyncJob, Object, String, Throwable)} or
     * {@link JobCallback#onCanceled(AsyncJob)} callback.
     * </li>
     * </ul>
     * <hr>
     * <p/>
     * The normally executor http request sample code as follow:
     * <blockquote><pre>
     *     HttpResponse performRequest(HttpJob job){
     *          ... do the core code for http request ...
     *          Http response = getResonse(job);
     *          return response;
     *     }
     * </pre></blockquote>
     * It's a success <code>JobExecutor</code>.
     * <hr>
     * A failed job's sample code as follow:
     * <blockquote><pre>
     *     HttpResponse performRequest(HttpJob job){
     *          ... do the core code for http request ...
     *          Http response = ... ops, some exception occurs, job running failed ...
     *          return response; // cannot running here
     *     }
     * </pre></blockquote>
     * If the exception is a instance of <code>UnRetryable</code> the do not do any retry yet, delivery method
     * {@link JobCallback#onFailed(AsyncJob, Object, String, Throwable)} callback.
     * <hr>
     * If current job get the response, but it is not the expected one, throw a <code>UnexpectedResponseException</code> with
     * unpexpected-value to notify the system that current is a failed job, it may cause invoke the
     * {@link JobCallback#onFailed(AsyncJob, Object, String, Throwable)} callback, which the second value is the
     * unexpected-value. The sample code as follow:
     * <blockquote><pre>
     *      HttpResponse performRequest(HttpJob job){
     *          ... do the core code for http request ...
     *          HttpResponse response = getResonse(job);
     *          if(response.isNotExpectedResponse){
     *              throw new UnexpectedResponseException().unexpectedResponse(response);
     *          }
     *          return response;
     *     }
     *     </pre></blockquote>
     * This job get the http response from server, But it's not the expected(such as http response 404), so throw the
     * exception will trigger failed callback.
     * </p>
     * <hr>
     *
     * @return the <b><i>expect</i></b> response.
     * @throws Throwable intterupt current job, running retry or post failed response.
     * @see UnexpectedResponseException
     * @see UnRetryable
     * @see UnRetryException
     */
    public abstract R performRequest() throws Throwable ;

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

    public void cancel() {
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

//    public void finish() {
//        // finish current job, dispose all resources has been holder.
//    }

    /**
     * Called when afrer delivery the response success.
     */
    public void markDelivered() {
        //TODO
    }

    public final void deliverCanceled() {
        CallbackDelivery delivery = getCallbackDelivery();
        if (null != delivery) {
            delivery.postCanceled(this);
        }
    }

    public final void deliverResponse(R response) {
        CallbackDelivery delivery = getCallbackDelivery();
        if (null != delivery) {
            delivery.postSuccess(this, response);
        }
    }

    public final void deliverError(R response, String msg, Throwable throwable) {
        CallbackDelivery delivery = getCallbackDelivery();
        if (null != delivery) {
            delivery.postFailed(this, response, msg, throwable);
        }
    }

    public final void deliverIntermediate(I intermediate) {
        CallbackDelivery delivery = getCallbackDelivery();
        if (null != delivery) {
            delivery.postIntermediate(this, intermediate);
        }
    }

    @Override
    public int compareTo(AsyncJob o) {
        return this.priorityPolicy.compareTo(o.priorityPolicy);
    }

//    @Override
//    public int hashCode() {
//
//        return fingerprint.hashCode();
//    }

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

    @Override
    public String fingerprint() {
        if (fingerprint == null) {
            return this.toString();
        }
        return fingerprint.fingerprint();
    }

//    public static class Builder<P, I, R> {
//        private P param;
//        private JobCallback callback;
//        private CallbackDelivery callbackDelivery = CallbackDelivery.DEFAULT_CALLBACK_DELIVERY;
//        private RetryPolicy retryPolicy = RetryPolicy.UnRetryPolicy;
//        private PriorityPolicy priorityPolicy = PriorityPolicy.DEFAULT_PRIORITY_POLICY;
//        private Fingerprint fingerprint = new SimpleFingerprint();
//
//        public Builder<P, I, R> setCallback(JobCallback callback) {
//            this.callback = callback;
//            return this;
//        }
//
//        public Builder<P, I, R> setParam(P param) {
//            this.param = param;
//            return this;
//        }
//
//        public Builder<P, I, R> setCallbackDelivery(CallbackDelivery callbackDelivery) {
//            this.callbackDelivery = callbackDelivery;
//            return this;
//        }
//
//        public Builder<P, I, R> setRetryPolicy(RetryPolicy retryPolicy) {
//            this.retryPolicy = retryPolicy;
//            return this;
//        }
//
//        public Builder<P, I, R> setPriorityPolicy(PriorityPolicy priorityPolicy) {
//            this.priorityPolicy = priorityPolicy;
//            return this;
//        }
//
//        public Builder<P, I, R> setFingerprint(Fingerprint fingerprint) {
//            this.fingerprint = fingerprint;
//            return this;
//        }
//
//        public Builder<P, I, R> disableReentrant() {
//            this.retryPolicy = RetryPolicy.UnRetryPolicy;
//            return this;
//        }
//
//        public AsyncJob<P, I, R> build() {
//            return new AsyncJob(param, callback, callbackDelivery, retryPolicy, priorityPolicy,
//                    fingerprint);
//        }
//    }
}
