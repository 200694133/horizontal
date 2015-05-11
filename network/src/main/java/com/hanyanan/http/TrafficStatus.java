package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/5/11.
 * Record the traffic status of current http request.
 */
public class TrafficStatus {
    /** The head size of out bound.  */
    private long outHeadBoundSize;
    /** The body size of out bound. */
    private long outBodyBoundSize;
    /** The head size of in bound. */
    private long inHeadBoundSize;
    /** The body size of in bound. */
    private long inBodyBoundSize;

    public TrafficStatus(){
        outHeadBoundSize = 0;
        outBodyBoundSize = 0;
        inHeadBoundSize = 0;
        inBodyBoundSize = 0;
    }

    public TrafficStatus(TrafficStatus other) {
        outHeadBoundSize = other.outHeadBoundSize;
        outBodyBoundSize = other.outBodyBoundSize;
        inHeadBoundSize = other.inHeadBoundSize;
        inBodyBoundSize = other.inBodyBoundSize;
    }

    /**
     * Return all the cost of current request.
     */
    public long getTrafficCost(){
        synchronized (this) {
            return outHeadBoundSize + outBodyBoundSize + inHeadBoundSize + inBodyBoundSize;
        }
    }
}
