package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/7.
 */
public interface FullPerformer {
    public AsyncJob nextRequest() throws InterruptedException;

    public void retry(AsyncJob asyncJob);

    /**
     * perform next request and delivery rsponse if success, or try to run again if it's failed.
     */
    public void fullPerformRequest();
}
