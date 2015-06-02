package com.hyn.scheduler;

/**
 * Created by hanyanan on 2015/5/31.
 */
public interface RequestExecutor<R> {
    /**
     * Perform current request and return the result.It's a execution unit.
     */
    public R performRequest(Request request) throws Throwable;
}
