package com.hyn.scheduler;

import com.hyn.scheduler.Request;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.Preconditions;
import hyn.com.lib.TimeUtils;

/**
 * Created by hanyanan on 2015/6/3.
 */
public class RequestQueue {
    public static final String TAG = "RequestDispatcher";
    public static final int DEFAULT_THREAD_POOL_SIZE = 4;

    /**
     * The set of all requests currently being processed by this RequestQueue. A Request
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
    private final PriorityBlockingQueue<Request> mCurrentRequests = new PriorityBlockingQueue<Request>();
    /**
     * The fingerprint and
     */
    private final Map<Fingerprint, Request> fingerprintRequestHashMap = new WeakHashMap<Fingerprint, Request>();
    private final int threadPoolSize;
    private final WorkerThreadExecutor []workerThreadExecutors;

    public RequestQueue(int threadPoolSize){
        this.threadPoolSize = threadPoolSize;
        workerThreadExecutors = new WorkerThreadExecutor[threadPoolSize];
    }

    public RequestQueue(){
        this(DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * Starts the dispatchers in this queue.
     */
    public synchronized void start(){
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < threadPoolSize; i++) {
            WorkerThreadExecutor workerThreadExecutor = new WorkerThreadExecutor(mCurrentRequests);
            workerThreadExecutors[i] = workerThreadExecutor;
            workerThreadExecutor.start();
        }
    }

    public synchronized void stop(){
        for (int i = 0; i < workerThreadExecutors.length; i++) {
            if (workerThreadExecutors[i] != null) {
                workerThreadExecutors[i].quit();
            }
        }
    }

    public synchronized void cancelAll(){
        mCurrentRequests.clear();
    }

    public synchronized void cancel(){

    }

    public synchronized void cancel(Fingerprint fingerprint){
        Preconditions.checkNotNull(fingerprint);
        Request request = null;
        request = fingerprintRequestHashMap.get(fingerprint);
        if(null != request) {
            request.cancel();
        }
    }

    public synchronized void add(Request request) {
        fingerprintRequestHashMap.put(request.getFingerprint(), request);
        mCurrentRequests.add(request);

        // Process requests in the order they are added.
        request.addMarker("add-to-queue");
        request.getRunningStatus().setLoadingTime(TimeUtils.getCurrentWallClockTime());
        request.setRequestStatus(RequestStatus.Pending);
    }

    /**
     * Called from {@link Request#finish(String)}, indicating that processing of the given request
     * has finished.
     *
     * <p>Releases waiting requests for <code>request.getCacheKey()</code> if
     *      <code>request.shouldCache()</code>.</p>
     */
    public synchronized void finish(Request request) {
        // Remove from the set of requests currently being processed.
        mCurrentRequests.remove(request);
    }
}
