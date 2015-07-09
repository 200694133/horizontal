package com.hyn.job;

/**
 * Created by hanyanan on 2015/5/31.
 */
public class RunningTrace {
    /** record the last add to queue time stamp */
    private long lastAddToQueueTimeStamp = 0;


    /** The times of failed.*/
    private int failedTimes = 0;
    /** pending time */
    private long totalPendingTime = 0;
    /** Totally run time. */
    private long totalRunningTime = 0;


    private long firstAddToQueueTimeStamp = 0;
    /** The time start running. */
    private long runningTimeStamp = 0;
    /** The time of finishing. */
    private long finishTimeStamp = 0;

    public long getTotalRunningTime() {
        return totalRunningTime;
    }

    public long getTotalPendingTime() {
        return totalPendingTime;
    }

    public int getFailedTimes() {
        return failedTimes;
    }

    public long getRunningTimeStamp() {
        return runningTimeStamp;
    }

    public long getFinishTimeStamp() {
        return finishTimeStamp;
    }

    RunningTrace(){

    }

    void failed(){
        failedTimes ++;
    }

    void setAddToQueueTimeStamp(long millTimes){
        if(this.firstAddToQueueTimeStamp <= 0){
            this.firstAddToQueueTimeStamp = millTimes;
        }
        lastAddToQueueTimeStamp = millTimes;
    }

    void setRunningTime(long millTimes){
        if(millTimes > lastAddToQueueTimeStamp) {
            this.totalPendingTime += (millTimes - lastAddToQueueTimeStamp);
        }
        this.runningTimeStamp = millTimes;
    }

    void setFinishTime(long millTimes){
        if(millTimes > runningTimeStamp) {
            this.totalRunningTime += (millTimes - runningTimeStamp);
        }
        this.finishTimeStamp = millTimes;
    }
}
