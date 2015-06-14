package com.hyn.job.group;

import com.hyn.job.AsyncJob;
import com.hyn.job.JobExecutor;
import hyn.com.lib.Preconditions;

/**
 * Created by hanyanan on 2015/6/4.
 */
public abstract class JobBatchExecutor<R> implements JobExecutor<AsyncJobBatch, R> {

    public abstract R performRequest(AsyncJobBatch request) throws Throwable;
}
