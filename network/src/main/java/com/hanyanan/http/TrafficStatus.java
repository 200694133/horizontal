package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/5/11.
 * Record the traffic status of current http request.
 */
public class TrafficStatus {
    public static final TrafficStatus sGlobalTrafficStatus = new TrafficStatus();

    public synchronized static TrafficStatus creator(){
        TrafficStatus trafficStatus = new TrafficStatus();




        return trafficStatus;
    }



    /** The head size of out bound.  */
    private long outHeadBoundSize;
    /** The body size of out bound. */
    private long outBodyBoundSize;
    /** The head size of in bound. */
    private long inHeadBoundSize;
    /** The body size of in bound. */
    private long inBodyBoundSize;
    /** The time cost during head information in. */
    private long inHeadCost;
    /** The time cost during send head information to server. */
    private long outHeadCost;
    /** The time cost during body was send. */
    private long inBodyCost;
    /** The time cost during head sending. */
    private long outBodyCost;

    private TrafficStatus(){
        outHeadBoundSize = 0;
        outBodyBoundSize = 0;
        inHeadBoundSize = 0;
        inBodyBoundSize = 0;
    }

    private TrafficStatus(TrafficStatus other) {
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

    public void headIn(long length, long timeCost){
        synchronized (this) {
            inHeadBoundSize += length;
        }
    }

    public void bodyIn(long length, long timeCost) {
        synchronized (this) {
            inBodyBoundSize += length;
        }
    }

    public void headOut(long length, long timeCost) {
        synchronized (this) {
            outHeadBoundSize += length;
        }
    }

    public void bodyOut(long length, long timeCost){
        synchronized (this) {
            outBodyBoundSize += length;
        }
    }

    public long getInBoundSize(){
        synchronized (this) {
            return inBodyBoundSize + inHeadBoundSize;
        }
    }

    public long getOutBoundSize() {
        synchronized (this) {
            return outBodyBoundSize + outHeadBoundSize;
        }
    }


    private interface TrafficListener {
        public void onHeadIn(long length, long cost);
        public void onHeadOut(long length, long cost);
        public void onBodyIn(long length, long cost);
        public void onBodyOut(long length, long cost);
    }
}
