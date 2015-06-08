package com.hyn.job;

/**
 * Created by hanyanan on 2015/5/31.
 */
public interface JobExecutor<R> {
    /**
     * Perform current request and return the result.It's a execution unit.
     */
    R performRequest(AsyncJob asyncJob) throws Throwable;
}
