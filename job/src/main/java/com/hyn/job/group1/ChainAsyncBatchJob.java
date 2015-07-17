package com.hyn.job.group1;

import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;
import com.hyn.job.PriorityPolicy;
import com.hyn.job.RetryPolicy;
import com.hyn.job.group.JobBatchExecutor;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/7/15.
 *
 */
public class ChainAsyncBatchJob<P> extends AsyncBatchJob<P> {
    public ChainAsyncBatchJob(JobLoader jobLoader, P param, JobCallback callback, CallbackDelivery callbackDelivery,
                              RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint) {
        super(jobLoader, param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint);
    }

    @Override
    public Void performRequest() throws Throwable {
        return null;
    }
}
