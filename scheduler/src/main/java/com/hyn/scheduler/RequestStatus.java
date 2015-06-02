package com.hyn.scheduler;

/**
 * Created by hanyanan on 2015/6/2.
 * A enum to record the current request status.
 */
public enum RequestStatus {
    /**
     * It's idle status, it forbid to running.
     */
    IDLE("IDLE"),
    /**
     * Current Request is in waiting queue, it's still waiting for running
     */
    Pending("Pending"),
    /**
     * Current Request is running.
     */
    Running("Running"),
    /**
     * Current request has finished, it may be successful/cancelled or error occurred.
     */
    Finish("Finish");
    /**
     * description for current status.
     */
    private final String status;
    private RequestStatus(String status){
        this.status = status;
    }

    @Override public String toString(){
        return status;
    }
}
