package com.hanyanan.http.internal;

/**
 * Created by hanyanan on 2015/5/14.
 */
public class Range {
    public final long start;
    public final long count;
    public final long fullLength;

    public Range(long start, long count, long fullLength){
        this.start = start;
        this.count = count;
        this.fullLength = fullLength;
    }
}
