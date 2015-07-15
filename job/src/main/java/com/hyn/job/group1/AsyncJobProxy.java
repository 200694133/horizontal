package com.hyn.job.group1;

import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.PriorityPolicy;
import com.hyn.job.RetryPolicy;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/7/13.
 */
class AsyncJobProxy<P, I, R> extends AsyncJob<P, I, R>{
    private final AsyncJob<P, I, R> asyncJob;
    AsyncJobProxy(AsyncJob<P, I, R> asyncJob, JobCallback callback){
        super(asyncJob.getParam(), callback, asyncJob.getCallbackDelivery(),
                asyncJob.getRetryPolicy(), asyncJob.getPriorityPolicy(),
                asyncJob.getFingerprint());
        this.asyncJob = asyncJob;
    }

    @Override
    public R performRequest() throws Throwable {
        return asyncJob.performRequest();
    }
}
