package com.hyn.job;

import java.util.LinkedList;
import java.util.List;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.SimpleFingerprint;

/**
 * Created by hanyanan on 2015/7/16.
 */
public class MultiProcessorAsyncJob<P, I, R> extends AsyncJob<P, I, R> {
    /**
     * The processor list to process current job.
     */
    private final List<JobProcessor> processorList = new LinkedList<JobProcessor>();


    public MultiProcessorAsyncJob(P param, JobCallback<I, R> callback, CallbackDelivery callbackDelivery,
                                  RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint,
                                  List<JobProcessor> processorList) {
        super(param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint);
        this.processorList.addAll(processorList);
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
    public R performRequest() throws Throwable {
        List<JobProcessor> processorList = new LinkedList<JobProcessor>(this.processorList);
        Object tmp = getParam();
        while (!processorList.isEmpty()) {
            JobProcessor processor = processorList.remove(0);
            tmp = processor.processor(this, tmp);
        }
        return (R) tmp;
    }


    public static class Builder<P, I, R> {
        private P param;
        private JobCallback callback;
        private CallbackDelivery callbackDelivery = CallbackDelivery.DEFAULT_CALLBACK_DELIVERY;
        private RetryPolicy retryPolicy = RetryPolicy.UnRetryPolicy;
        private PriorityPolicy priorityPolicy = PriorityPolicy.DEFAULT_PRIORITY_POLICY;
        private Fingerprint fingerprint = new SimpleFingerprint();
        private final List<JobProcessor> processorList = new LinkedList<JobProcessor>();

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

        public Builder<P, I, R> setFingerprint(Fingerprint fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public Builder<P, I, R> disableReentrant() {
            this.retryPolicy = RetryPolicy.UnRetryPolicy;
            return this;
        }

        public Builder<P, I, R> addProcessor(JobProcessor processor){
            this.processorList.add(processor);
            return this;
        }

        public MultiProcessorAsyncJob<P, I, R> build() {
            return new MultiProcessorAsyncJob(param, callback, callbackDelivery, retryPolicy, priorityPolicy,
                    fingerprint, processorList);
        }
    }
}
