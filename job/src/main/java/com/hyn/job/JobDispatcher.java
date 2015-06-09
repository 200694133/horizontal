package com.hyn.job;


import com.hyn.job.impl.DefaultCallbackDelivery;
import com.hyn.job.log.Log;

import java.util.concurrent.BlockingQueue;


/**
 * Created by hanyanan on 2015/6/3.
 * <p/>
 * Provides a thread for performing network dispatch from a queue of requests.
 */
public class JobDispatcher extends Thread implements FullPerformer{
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
                if(asyncJob == null) {
                    return ;
                }
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (isQuit()) {
                    return;
                }
                continue;
            }
            RunningTrace runningTrace = asyncJob.getRunningTrace();
            CallbackDelivery delivery = asyncJob.getCallbackDelivery();
            JobExecutor jobExecutor = asyncJob.getJobExecutor();
            if (null == delivery) delivery = new DefaultCallbackDelivery();
            asyncJob.addMarker("network-queue-take");

            // If the request was cancelled already, do not perform the current request.
            if (asyncJob.isCanceled()) {
                asyncJob.addMarker("network-discard-cancelled");
                asyncJob.setJobStatus(JobStatus.Finish);
                delivery.postCanceled(asyncJob);
                continue;
            }
            asyncJob.setJobStatus(JobStatus.Running);
            runningTrace.setRunningTime(currentTimeMillis());
            try {
                asyncJob.addMarker("network-start-running");
                Object response = jobExecutor.performRequest(asyncJob);
                if (isQuit()) {
                    return;
                }
                asyncJob.setJobStatus(JobStatus.Finish);
                if (asyncJob.isCanceled()) {
                    asyncJob.addMarker("network-discard-cancelled");
                    delivery.postCanceled(asyncJob);
                    continue;
                }
                runningTrace.setFinishTime(System.currentTimeMillis());
                asyncJob.addMarker("request-complete");
                delivery.postSuccess(asyncJob, response);
                asyncJob.markDelivered();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (isQuit()) {
                    return;
                }
                runningTrace.failed();
                RetryPolicy retryPolicy = asyncJob.getRetryPolicy();
                if (retryPolicy.retry(asyncJob, throwable)) { // retry again.
                    // change the priority of current request.
                    asyncJob.setPriorityPolicy(retryPolicy.retryPriority(asyncJob, asyncJob.getPriorityPolicy()));
                    retry(asyncJob);
                    asyncJob.setJobStatus(JobStatus.IDLE);
                    runningTrace.setAddToQueueTimeStamp(currentTimeMillis());
                } else { // failed
                    asyncJob.setJobStatus(JobStatus.Finish);
                    delivery.postFailed(asyncJob, null, throwable);
                    runningTrace.setFinishTime(currentTimeMillis());
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
