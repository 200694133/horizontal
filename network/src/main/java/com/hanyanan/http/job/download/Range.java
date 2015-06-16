package com.hanyanan.http.job.download;

/**
 * Created by hanyanan on 2015/6/16.
 */
public class Range {
    public long offset;
    public long length;
    public Range(long offset, long length){
        this.offset = offset;
        this.length = length;
    }
    boolean deliveryed = false;
}
