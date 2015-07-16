package com.hyn.job;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/6/2.
 */
public class JobLoader {
    private static JobLoader sLoadInstance = null;

    public static synchronized JobLoader getInstance() {
        if (null == sLoadInstance) {
            sLoadInstance = new JobLoader();
        }
        return sLoadInstance;
    }

    private final List<JobActivityLifecycleCallbacks> jobActivityLifecycleCallbacksList = new ArrayList<JobActivityLifecycleCallbacks>();
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    private JobQueue jobQueue = new JobQueue();

    private JobLoader() {
        jobQueue.start();
    }

    public synchronized void addJobActivityLifecycleCallbacks(JobActivityLifecycleCallbacks callbacks) {
        if (!jobActivityLifecycleCallbacksList.contains(callbacks)) {
            jobActivityLifecycleCallbacksList.add(callbacks);
        }
    }
    

    public synchronized void load(AsyncJob asyncJob) {
        jobQueue.add(asyncJob);
    }

    public synchronized void loadDelayed(AsyncJob asyncJob, TimeUnit timeUnit, long time) {
        //TODO
    }

    public synchronized void loadDelayed(AsyncJob asyncJob, long millTimes) {
        //TODO
    }

    public synchronized void loadSchedule(AsyncJob asyncJob, TimeUnit timeUnit, long timeInterval, int count) {
        //TODO
    }

    public synchronized void loadSchedule(AsyncJob asyncJob, long timeInterval) {
        LoadRunnable runnable = new LoadRunnable(scheduledThreadPoolExecutor, jobQueue, asyncJob);
        scheduledThreadPoolExecutor.schedule(runnable, timeInterval, TimeUnit.MILLISECONDS);
        scheduledThreadPoolExecutor.remove(runnable);
    }

    public synchronized void cancel(AsyncJob asyncJob) {

    }


    private static class LoadRunnable implements Runnable {
        private final WeakReference<JobQueue> jobQueueWeakReference;
        private final WeakReference<AsyncJob> jobWeakReference;
        private final WeakReference<ScheduledThreadPoolExecutor> scheduledExecutorWeakReference;
        private final int runningCount;
        private int currentRunningCount = 0;

        private LoadRunnable(ScheduledThreadPoolExecutor executor, JobQueue jobQueue, AsyncJob job) {
            this(executor, jobQueue, job, 1);
        }

        private LoadRunnable(ScheduledThreadPoolExecutor executor, JobQueue jobQueue, AsyncJob job, int runningCount) {
            jobQueueWeakReference = new WeakReference<JobQueue>(jobQueue);
            jobWeakReference = new WeakReference<AsyncJob>(job);
            this.runningCount = runningCount;
            scheduledExecutorWeakReference = new WeakReference<ScheduledThreadPoolExecutor>(executor);
        }

        public void run() {
            if (null == jobQueueWeakReference.get() || null == jobWeakReference.get()) return;
            jobQueueWeakReference.get().add(jobWeakReference.get());
        }
    }
}
