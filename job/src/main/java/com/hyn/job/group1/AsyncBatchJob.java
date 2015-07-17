package com.hyn.job.group1;

import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;
import com.hyn.job.PriorityPolicy;
import com.hyn.job.RetryPolicy;
import com.hyn.job.group.JobBatchExecutor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/5/31.
 */
public abstract class AsyncBatchJob<P> extends AsyncJob<P, BatchJobProgress, Void>  {
    protected final List<AsyncJob> asyncJobList = new LinkedList<AsyncJob>();
    protected final JobLoader jobLoader;
    public AsyncBatchJob(JobLoader jobLoader, P param, JobCallback callback, CallbackDelivery callbackDelivery,
                         RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint) {
        super(param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint);
        this.jobLoader = jobLoader;
    }

    public List<AsyncJob> getChildren(){
        return Collections.unmodifiableList(asyncJobList);
    }

    public void add(AsyncJob asyncJob){
        synchronized (asyncJobList) {
            asyncJobList.add(asyncJob);
        }
    }

    public int getChildCount(){
        return asyncJobList.size();
    }

    public AsyncJob indexOf(int index){
        synchronized (asyncJobList) {
            if(index >= asyncJobList.size()) {
                return null;
            }

            return asyncJobList.get(index);
        }
    }


    public AsyncJob remove(int index){
        synchronized (asyncJobList) {
            if(index >= asyncJobList.size()){
                return null;
            }

            return asyncJobList.remove(index);
        }
    }



    public void remove(AsyncJob asyncJob){
        synchronized (asyncJobList) {
            asyncJobList.remove(asyncJob);
        }
    }
}
