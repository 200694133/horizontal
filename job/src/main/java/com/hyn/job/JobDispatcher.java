package com.hyn.job;


import com.hyn.job.log.Log;

import java.util.concurrent.BlockingQueue;

import hyn.com.lib.TimeUtils;


/**
 * Created by hanyanan on 2015/6/3.
 * <p/>
 * Provides a thread for performing network dispatch from a queue of requests.
 */
public class JobDispatcher extends Thread implements FullPerformer {
    private static final String TAG = "WorkerThreadExecutor";
    /**
     * The queue of requests to service.
     */
    protected final BlockingQueue<AsyncJob> queue;

    @Override
    public AsyncJob nextJob() throws InterruptedException {
        return queue.take();
    }

    @Override
    public void retry(AsyncJob asyncJob) {
        queue.add(asyncJob);
    }

    @Override
    public void fullPerformRequest() {
        AsyncJob asyncJob;
        while (true) {
            if (isQuit()) {
                return;
            }
            try {
                // Take a request from the queue.
                asyncJob = nextJob();
                if (isQuit()) {
                    return;
                }
                if (asyncJob == null) {
                    return;
                }
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (isQuit()) {
                    return;
                }
                continue;
            }
            RunningTrace runningTrace = asyncJob.getRunningTrace();
            JobExecutor jobExecutor = asyncJob.getJobExecutor();
            asyncJob.addMarker("job-queue-take");
            runningTrace.setRunningTime(TimeUtils.getCurrentWallClockTime());

            // If the request was cancelled already, do not perform the current request.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("job-discard-cancelled");
                asyncJob.setJobStatus(JobStatus.Finish);
                runningTrace.setFinishTime(TimeUtils.getCurrentWallClockTime());
                asyncJob.deliverCanceled();
                continue;
            }
            Object response = null;
            try {
                asyncJob.setJobStatus(JobStatus.Running);
                asyncJob.addMarker("job-start-running");
                if (null != jobExecutor) {
                    response = jobExecutor.performRequest(asyncJob);
                } else {
                    response = asyncJob.performRequest();
                }
                runningTrace.setFinishTime(TimeUtils.getCurrentWallClockTime());
                asyncJob.setJobStatus(JobStatus.Finish);
                if (asyncJob.isCanceled()) {
                    asyncJob.addMarker("network-discard-cancelled");
                    asyncJob.deliverCanceled();
                    continue;
                }
                asyncJob.addMarker("job-complete");
                if (isQuit()) {
                    return;
                }
                asyncJob.deliverResponse(response);
                asyncJob.markDelivered();
            } catch (UnexpectedResponseException exception) {
                /*
                * force make failed.
                * */
                runningTrace.failed();
                runningTrace.setFinishTime(TimeUtils.getCurrentWallClockTime());
                asyncJob.setJobStatus(JobStatus.Finish);
                Object tmp = null;
                if (null != exception.getUnexpectedResponse()) {
                    tmp = exception.getUnexpectedResponse().getValue();
                }
                asyncJob.deliverError(tmp, null, exception);
                continue;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                runningTrace.failed();
                runningTrace.setFinishTime(TimeUtils.getCurrentWallClockTime());
                asyncJob.setJobStatus(JobStatus.Finish);
                if (isQuit()) {
                    return;
                }

                if (throwable instanceof UnRetryable) {
                    /*
                    * Cannot running again. execute failed.
                    * */
                    asyncJob.deliverError(null, null, throwable);
                    continue;
                }

                RetryPolicy retryPolicy = asyncJob.getRetryPolicy();
                if (retryPolicy.retry(asyncJob, throwable)) { // retry again.
                    // change the priority of current request.
                    asyncJob.setPriorityPolicy(retryPolicy.retryPriority(asyncJob, asyncJob.getPriorityPolicy()));
                    retry(asyncJob);
                    asyncJob.setJobStatus(JobStatus.IDLE);
                    runningTrace.setAddToQueueTimeStamp(TimeUtils.getCurrentWallClockTime());
                } else { // failed
                    asyncJob.deliverError(null, null, throwable);
                }
                continue;
            }
        }

    }

    private enum Status {
        IDLE, RUNNING, PAUSE, QUIT;
    }

    /**
     * Used for telling us to die.
     */
    private volatile Status status = Status.IDLE;

    protected JobDispatcher(BlockingQueue<AsyncJob> queue) {
        this.queue = queue;
    }

    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        synchronized (this) {
            status = Status.QUIT;
        }
        interrupt();
    }

//    public void pause(){
//        //TODO check status
//        synchronized (this) {
//            status = Status.PAUSE;
//        }
//    }
//
//    public void go(){
//        //TODO check status
//        synchronized (this) {
//            status = Status.RUNNING;
//            this.notify();
//        }
//    }
//
//    private void checkPause() throws InterruptedException {
//        synchronized (this) {
//            if(status == Status.PAUSE){
//                this.wait();
//            }
//        }
//    }

    private boolean isQuit() {
        synchronized (this) {
            if (status == Status.QUIT) {
                Log.d(TAG, "Quit current thread " + this.toString());
                return true;
            }
            return false;
        }
    }

    public void run() {
        fullPerformRequest();
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
