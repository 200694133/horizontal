package com.hyn.scheduler;


import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.Preconditions;
import hyn.com.lib.TimeUtils;

/**
 * Created by hanyanan on 2015/6/3.
 */
class RequestQueue {
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
    private final RequestDispatcher[] requestDispatchers;

    RequestQueue(int threadPoolSize){
        this.threadPoolSize = threadPoolSize;
        requestDispatchers = new RequestDispatcher[threadPoolSize];
    }

    RequestQueue(){
        this(DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * Starts the dispatchers in this queue.
     */
    public synchronized void start(){
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < threadPoolSize; i++) {
            RequestDispatcher requestDispatcher = new RequestDispatcher(mCurrentRequests);
            requestDispatchers[i] = requestDispatcher;
            requestDispatcher.start();
        }
    }

    public synchronized void stop(){
        for (int i = 0; i < requestDispatchers.length; i++) {
            if (requestDispatchers[i] != null) {
                requestDispatchers[i].quit();
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
