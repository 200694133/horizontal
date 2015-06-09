package com.hyn.job;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/6/9.
 */
public class SimpleAsyncJob<P, I, R> extends AsyncJob<P, I, R> implements JobCallback<I, R>

{
    public SimpleAsyncJob(P param, JobCallback callback, CallbackDelivery callbackDelivery,
                          RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, JobExecutor<R> jobExecutor) {
        super(param, callback, callbackDelivery, retryPolicy, new Builder().build(), priorityPolicy,  jobExecutor);
    }



    @Override
    public void onCanceled(AsyncJob asyncJob) {

    }

    @Override
    public void onSuccess(AsyncJob asyncJob, R response) {

    }

    @Override
    public void onFailed(AsyncJob asyncJob, String msg, Throwable throwable) {

    }

    @Override
    public void onIntermediate(I intermediate) {

    }
}
