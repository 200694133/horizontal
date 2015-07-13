package com.hyn.job.group1;

import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;
import com.hyn.job.PriorityPolicy;
import com.hyn.job.RetryPolicy;
import com.hyn.job.group.JobBatchExecutor;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/7/13.
 */
public class SerialJobBatchExecutor<P> extends AsyncBatchJob<P> implements JobCallback {
    public SerialJobBatchExecutor(JobLoader jobLoader, P param, JobCallback callback, CallbackDelivery callbackDelivery,
                                  RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint,
                                  JobBatchExecutor requestExecutor) {
        super(jobLoader, param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint, requestExecutor);
    }

    @Override
    public Void performRequest() throws Throwable {
        return null;
    }

    @Override
    public void onCanceled(AsyncJob asyncJob) {

    }

    @Override
    public void onSuccess(AsyncJob asyncJob, Object response) {
        if(null != asyncJob.getCallback()) {
            asyncJob.getCallback().onSuccess(asyncJob, response);
        }
    }

    @Override
    public void onFailed(AsyncJob asyncJob, Object response, String msg, Throwable throwable) {
        if(null != asyncJob.getCallback()) {
            asyncJob.getCallback().onFailed(asyncJob, response, msg,throwable);
        }
    }

    @Override
    public void onIntermediate(AsyncJob asyncJob, Object intermediate) {
        if(null != asyncJob.getCallback()) {
            asyncJob.getCallback().onIntermediate(asyncJob, intermediate);
        }
    }
}