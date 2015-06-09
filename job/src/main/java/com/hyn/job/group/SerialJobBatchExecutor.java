package com.hyn.job.group;

import com.hyn.job.FullPerformer;
import com.hyn.job.AsyncJob;
import com.hyn.job.JobDispatcher;
import com.hyn.job.JobLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by hanyanan on 2015/6/2.
 */
public class SerialJobBatchExecutor extends JobBatchExecutor<Void> {
    FullPerformer fullPerformer;
    @Override
    public Void performRequest(AsyncJobBatch request) throws Throwable {
        List<AsyncJob> asyncJobList = new ArrayList<AsyncJob>(request.getChildren());
        JobLoader jobLoader = request.jobLoader;
        if(null == asyncJobList) {
            return null;
        }
        if(null == jobLoader) {
            throw new IllegalArgumentException("");
        }

        final BlockingQueue<AsyncJob> blockingQueue = new LinkedBlockingQueue<AsyncJob>(asyncJobList);
        fullPerformer = new JobDispatcher(blockingQueue){
            @Override
            public AsyncJob nextJob() throws InterruptedException {
                return queue.poll();
            }
        };
        fullPerformer.fullPerformRequest();
        return null;
    }
}
