package com.hyn.job;

/**
 * Created by hanyanan on 2015/5/31.
 */
public class PriorityPolicy implements Comparable<PriorityPolicy>{
    public final static PriorityPolicy DEFAULT_PRIORITY_POLICY = new PriorityPolicy(){

    };
    @Override
    public int compareTo(PriorityPolicy o) {
        return 0;
    }
}
