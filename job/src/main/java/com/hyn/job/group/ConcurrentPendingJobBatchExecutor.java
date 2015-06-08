package com.hyn.job.group;

import com.hyn.job.AsyncJob;

/**
 * Created by hanyanan on 2015/6/7.
 */
public class ConcurrentPendingJobBatchExecutor extends JobBatchExecutor {

    public AsyncJob getPendingRequest(){
        return null;
    }
    @Override
    public Object performRequest(AsyncJobBatch request) throws Throwable {
        return null;
    }

    public void onRequestSuccess(){

    }
}
