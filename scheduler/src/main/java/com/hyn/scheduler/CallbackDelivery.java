package com.hyn.scheduler;

/**
 * Created by hanyanan on 2015/6/2.
 */
public interface CallbackDelivery {
    /**
     * Post the result to the request
     */
    <R> void postSuccess(Request<?, ?, R> request, R result);

    /**
     * @param request
     */
    void postCanceled(Request request);

    /**
     * @param request
     * @param msg
     * @param throwable
     */
    void postFailed(Request request, String msg, Throwable throwable);

    /**
     *
     */
    <T> void postIntermediate(Request<?, T, ?> request, T intermediate);
}
