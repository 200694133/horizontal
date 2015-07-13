package com.hyn.job.group1;

import com.hyn.job.AsyncJob;
import com.hyn.job.JobCallback;

/**
 * Created by hanyanan on 2015/7/13.
 */
class AsyncJobProxy<P, I, R> extends AsyncJob<P, I, R>{
    private final AsyncJob<P, I, R> asyncJob;
    AsyncJobProxy(AsyncJob<P, I, R> asyncJob, JobCallback callback){
        super();
    }

    @Override
    public R performRequest() throws Throwable {
        return null;
    }
}
