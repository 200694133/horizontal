package com.hyn.job.group1;

import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;
import com.hyn.job.PriorityPolicy;
import com.hyn.job.RetryPolicy;
import com.hyn.job.group.JobBatchExecutor;

import java.util.Collections;
import java.util.List;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/7/13.
 */
public class ConcurrentAsyncBatchJob<P> extends AsyncBatchJob<P> {
    public ConcurrentAsyncBatchJob(JobLoader jobLoader, P param, JobCallback callback,
                                   CallbackDelivery callbackDelivery, RetryPolicy retryPolicy,
                                   PriorityPolicy priorityPolicy, Fingerprint fingerprint) {
        super(jobLoader, param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint);
    }

    @Override
    public Void performRequest() throws Throwable {
        List<AsyncJob> asyncJobList = Collections.unmodifiableList(getChildren());
        JobLoader jobLoader = this.jobLoader;
        if(null == asyncJobList || asyncJobList.isEmpty()) {
            return null;
        }

        for(AsyncJob req : asyncJobList){
            jobLoader.load(req);
        }
        return null;
    }
}
