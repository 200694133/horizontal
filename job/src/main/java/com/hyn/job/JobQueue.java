package com.hyn.job;


import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.Preconditions;
import hyn.com.lib.TimeUtils;

/**
 * Created by hanyanan on 2015/6/3.
 */
class JobQueue {
    public static final String TAG = "RequestDispatcher";
    public static final int DEFAULT_THREAD_POOL_SIZE = 4;

    /**
     * The set of all requests currently being processed by this RequestQueue. A Request
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
    private final PriorityBlockingQueue<AsyncJob> mCurrentAsyncJobs = new PriorityBlockingQueue<AsyncJob>();
    /**
     * The fingerprint and
     */
    private final Map<Fingerprint, AsyncJob> fingerprintRequestHashMap = new WeakHashMap<Fingerprint, AsyncJob>();
    private final int threadPoolSize;
    private final JobDispatcher[] jobDispatchers;

    JobQueue(int threadPoolSize){
        this.threadPoolSize = threadPoolSize;
        jobDispatchers = new JobDispatcher[threadPoolSize];
    }

    JobQueue(){
        this(DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * Starts the dispatchers in this queue.
     */
    public synchronized void start(){
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < threadPoolSize; i++) {
            JobDispatcher jobDispatcher = new JobDispatcher(mCurrentAsyncJobs);
            jobDispatchers[i] = jobDispatcher;
            jobDispatcher.start();
        }
    }

    public synchronized void stop(){
        for (int i = 0; i < jobDispatchers.length; i++) {
            if (jobDispatchers[i] != null) {
                jobDispatchers[i].quit();
            }
        }
    }

    public synchronized void cancelAll(){
        mCurrentAsyncJobs.clear();
    }

    public synchronized void cancel(){

    }

    public synchronized void cancel(Fingerprint fingerprint){
        Preconditions.checkNotNull(fingerprint);
        AsyncJob asyncJob = null;
        asyncJob = fingerprintRequestHashMap.get(fingerprint);
        if(null != asyncJob) {
            asyncJob.cancel();
        }
    }

    public synchronized void add(AsyncJob asyncJob) {
        fingerprintRequestHashMap.put(asyncJob.getFingerprint(), asyncJob);
        mCurrentAsyncJobs.add(asyncJob);

        // Process requests in the order they are added.
        asyncJob.addMarker("add-to-queue");
        asyncJob.getRunningTrace().setLoadingTime(TimeUtils.getCurrentWallClockTime());
        asyncJob.setJobStatus(JobStatus.Pending);
    }

    /**
     * Called from {@link AsyncJob#finish(String)}, indicating that processing of the given request
     * has finished.
     *
     * <p>Releases waiting requests for <code>request.getCacheKey()</code> if
     *      <code>request.shouldCache()</code>.</p>
     */
    public synchronized void finish(AsyncJob asyncJob) {
        // Remove from the set of requests currently being processed.
        mCurrentAsyncJobs.remove(asyncJob);
    }
}
