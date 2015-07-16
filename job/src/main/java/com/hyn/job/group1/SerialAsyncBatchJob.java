package com.hyn.job.group1;

import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;
import com.hyn.job.PriorityPolicy;
import com.hyn.job.RetryPolicy;
import com.hyn.job.group.JobBatchExecutor;

import java.util.ArrayList;
import java.util.List;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/7/13.
 */
public class SerialAsyncBatchJob<P> extends AsyncBatchJob<P> implements JobCallback {
    private final List<AsyncJob> finishedJobList = new ArrayList<AsyncJob>();
    private final List<AsyncJob> waitingJobList = new ArrayList<AsyncJob>();
    private AsyncJob pendingJob;
    public SerialAsyncBatchJob(JobLoader jobLoader, P param, JobCallback callback, CallbackDelivery callbackDelivery) {
        super(jobLoader, param, callback, callbackDelivery, RetryPolicy.UnRetryPolicy,
                PriorityPolicy.DEFAULT_PRIORITY_POLICY, Fingerprint.DEFAULT_FINGERPRINT, null);
    }

    public SerialAsyncBatchJob(JobLoader jobLoader, P param, JobCallback callback) {
        super(jobLoader, param, callback, CallbackDelivery.DEFAULT_CALLBACK_DELIVERY, RetryPolicy.UnRetryPolicy,
                PriorityPolicy.DEFAULT_PRIORITY_POLICY, Fingerprint.DEFAULT_FINGERPRINT, null);
    }

    @Override
    public Void performRequest() throws Throwable {
        waitingJobList.addAll(asyncJobList);
        scheduleNext();
        return null;
    }

    @Override
    public synchronized void cancel() {
        for(AsyncJob job : waitingJobList){
            job.cancel();
        }
        pendingJob.cancel();
        super.cancel();
    }

    private synchronized void scheduleNext(){
        if(waitingJobList.isEmpty()) {
            pendingJob = null;
            deliverResponse(null);
            return ;
        }

        pendingJob = waitingJobList.remove(0);

        jobLoader.load(pendingJob);

        deliverIntermediate(new BatchJobProgress() {
            @Override
            public AsyncJob onJobFinish() {
                return pendingJob;
            }
        });
    }

    @Override
    public void onCanceled(AsyncJob asyncJob) {
        if(null != asyncJob.getCallback()) {
            asyncJob.getCallback().onCanceled(asyncJob);
        }
        deliverCanceled();
    }

    @Override
    public void onSuccess(AsyncJob asyncJob, Object response) {
        finishedJobList.add(asyncJob);
        if(null != asyncJob.getCallback()) {
            asyncJob.getCallback().onSuccess(asyncJob, response);
        }
        // load next
        scheduleNext();
    }


    @Override
    public void onFailed(AsyncJob asyncJob, Object response, String msg, Throwable throwable) {
        if(null != asyncJob.getCallback()) {
            asyncJob.getCallback().onFailed(asyncJob, response, msg,throwable);
        }
        deliverError(null, msg, throwable);
    }

    @Override
    public void onIntermediate(AsyncJob asyncJob, Object intermediate) {
        if(null != asyncJob.getCallback()) {
            asyncJob.getCallback().onIntermediate(asyncJob, intermediate);
        }
    }
}
