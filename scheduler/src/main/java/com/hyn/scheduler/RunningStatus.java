package com.hyn.scheduler;

/**
 * Created by hanyanan on 2015/5/31.
 */
public class RunningStatus {
    /** The times of failed.*/
    private int failedTimes;
    /** Real running time. */
    private long realRunningTime;
    /** pending time */
    private long pendingTime;
    /** the time when load to request queue. */
    private long loadTimeStamp;
    /** The time start running. */
    private long runningTimeStamp;
    /** The time of finishing. */
    private long finishTimeStamp;
}
