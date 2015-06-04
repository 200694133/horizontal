package com.hyn.scheduler;


import com.hyn.scheduler.impl.DefaultCallbackDelivery;
import com.hyn.scheduler.log.Log;

import java.util.concurrent.BlockingQueue;


/**
 * Created by hanyanan on 2015/6/3.
 * <p/>
 * Provides a thread for performing network dispatch from a queue of requests.
 */
public class WorkerThreadExecutor extends Thread {
    private static final String TAG = "WorkerThreadExecutor";
    /**
     * The queue of requests to service.
     */
    private final BlockingQueue<Request> queue;

    private enum Status {
        IDLE, RUNNING, PAUSE, QUIT;
    }

    /**
     * Used for telling us to die.
     */
    private volatile Status status = Status.IDLE;

    public WorkerThreadExecutor(BlockingQueue<Request> queue) {
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
        Request request;
        while (true) {
            if (isQuit()) {
                return;
            }
            try {
                // Take a request from the queue.
                request = queue.take();
                if (isQuit()) {
                    return;
                }
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (isQuit()) {
                    return;
                }
                continue;
            }
            RunningStatus runningStatus = request.getRunningStatus();
            CallbackDelivery delivery = request.getCallbackDelivery();
            RequestExecutor requestExecutor = request.getRequestExecutor();
            if (null == delivery) delivery = new DefaultCallbackDelivery();
            request.addMarker("network-queue-take");

            // If the request was cancelled already, do not perform the current request.
            if (request.isCanceled()) {
                request.addMarker("network-discard-cancelled");
                delivery.postCanceled(request);
                continue;
            }
            runningStatus.setRunningTime(currentTimeMillis());
            try {
                request.addMarker("network-start-running");
                Object response = requestExecutor.performRequest(request);
                if (isQuit()) {
                    return;
                }
                if (request.isCanceled()) {
                    request.addMarker("network-discard-cancelled");
                    delivery.postCanceled(request);
                    continue;
                }
                runningStatus.setFinishTime(System.currentTimeMillis());
                request.addMarker("request-complete");
                delivery.postSuccess(request, response);
                request.markDelivered();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (isQuit()) {
                    return;
                }
                runningStatus.failed();
                RetryPolicy retryPolicy = request.getRetryPolicy();
                if (retryPolicy.retry(request, throwable)) { // retry again.
                    // change the priority of current request.
                    request.setPriorityPolicy(retryPolicy.retryPriority(request, request.getPriorityPolicy()));
                    queue.add(request);
                    runningStatus.setAddToQueueTimeStamp(currentTimeMillis());
                } else { // failed
                    delivery.postFailed(request, null, throwable);
                    runningStatus.setFinishTime(currentTimeMillis());
                }
                continue;
            }
        }
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
