package com.hyn.scheduler;

import java.util.Queue;

/**
 * Created by hanyanan on 2015/6/7.
 */
public interface FullPerformer {
    public Request nextRequest() throws InterruptedException;

    public void retry(Request request);

    /**
     * perform next request and delivery rsponse if success, or try to run again if it's failed.
     */
    public void fullPerformRequest();
}
