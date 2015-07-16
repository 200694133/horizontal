package com.hyn.job;

/**
 * Created by hanyanan on 2015/7/15.
 */
public interface JobFiber<INPUT, OUTPUT> {
    /**
     * A processor accept a input param and return a value as the result.
     * Note that, when do the background work, it mast be cancel current job when asyncJob has canceled.
     * @param asyncJob the job has bind to current processor, it recommend to canceled current job when this job has
     *                 been canceled.
     * @param input
     * @return
     */
    public OUTPUT processor(AsyncJob asyncJob, INPUT input) throws Throwable;
}
