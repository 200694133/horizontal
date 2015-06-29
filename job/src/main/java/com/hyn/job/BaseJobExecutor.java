package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/29.
 */
public abstract class BaseJobExecutor<J extends AsyncJob, R> implements JobExecutor<J, R> {

    /**
     * Abort current job and retry running again.
     * @param job
     * @throws Throwable
     */
    protected void retry(J job) throws Throwable{
        throw new RetryException();
    }

    protected void unexpectedResponseError(String msg, Throwable throwable, R unexpectedResponse) throws UnexpectedResponseException {
        UnexpectedResponseException exception = new UnexpectedResponseException(msg, throwable);
        exception.unexpectedResponse(new UnexpectedResponseException.Result<R>(unexpectedResponse));
        throw exception;
    }

    protected void error() throws UnRetryException {
        throw new UnRetryException();
    }
}