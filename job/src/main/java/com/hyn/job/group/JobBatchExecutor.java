package com.hyn.job.group;


/**
 * Created by hanyanan on 2015/6/4.
 */
public abstract class JobBatchExecutor<R> {

    public abstract R performRequest(AsyncJobBatch request) throws Throwable;
}
