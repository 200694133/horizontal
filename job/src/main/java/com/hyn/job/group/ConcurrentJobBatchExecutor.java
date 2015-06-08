package com.hyn.job.group;

import com.hyn.job.AsyncJob;
import com.hyn.job.JobLoader;

import java.util.Collections;
import java.util.List;

/**
 * Created by hanyanan on 2015/6/2.
 */
public class ConcurrentJobBatchExecutor extends JobBatchExecutor<Void> {
    @Override
    public Void performRequest(AsyncJobBatch request) throws Throwable {
        List<AsyncJob> asyncJobList = Collections.unmodifiableList(request.getChildren());
        JobLoader jobLoader = request.jobLoader;
        if(null == asyncJobList) {
            return null;
        }
        if(null == jobLoader) {
            throw new IllegalArgumentException("");
        }

        for(AsyncJob req : asyncJobList){
            jobLoader.load(req);
        }
        return null;
    }
}
