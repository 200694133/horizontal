package com.hyn.job;

/**
 * Created by hanyanan on 2015/5/31.
 * This interface is the core runnig executor part for a job, it accept input job, and output the response.<br>
 * This interface is the core part of a job, any job want to work normally must contain a class implements this
 * interface. <br>
 * A good design for a asyn job architecture need reuse and simple implements. Most of job just implement current
 * interface.
 */
public interface JobExecutor<J extends AsyncJob, R> {

    /**
     * Perform current request and return the result.It's a execution unit. If the input job is a
     * <br>
     *
     * User can throw some specify Throwable to retry again or force delivery failed result.
     * <hr>
     * <ul>
     *     <li>{@link UnexpectedResponseException} may be attaching a unexpected response,
     *          this will trigger {@link com.hyn.job.JobCallback#onFailed(AsyncJob, Object, String, Throwable)} if
     *          current Job not canceled!
     *     </li>
     *     <li>if user throw any exception which implements {@link UnRetryable} interface, it means current job has
     *          be abandoned, notify {@link JobCallback#onFailed(AsyncJob, Object, String, Throwable)} or
     *          {@link JobCallback#onCanceled(AsyncJob)} callback.
     *     </li>
     *     <li>
     *         {@link UnRetryException} is a runtime exception, which implement {@code UnRetryable}. This is
     *         default exception for interrupt current job and do not retry again, will notify
     *         {@link JobCallback#onFailed(AsyncJob, Object, String, Throwable)} or
     *         {@link JobCallback#onCanceled(AsyncJob)} callback.
     *     </li>
     * </ul>
     * <hr>
     *
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
     *     A failed job's sample code as follow:
     *     <blockquote><pre>
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
     * @see UnexpectedResponseException
     * @see UnRetryable
     * @see UnRetryException
     * @param asyncJob the specify job will be running.
     * @return the <b><i>expect</i></b> response.
     * @throws Throwable intterupt current job, running retry or post failed response.
     */
    R performRequest(J asyncJob) throws Throwable;
}
