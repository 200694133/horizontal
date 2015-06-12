package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/2.
 */
public interface CallbackDelivery {
    public final static CallbackDelivery DEFAULT_CALLBACK_DELIVERY = new DefaultCallbackDelivery();

    /**
     * Post the result to the request
     */
    <R> void postSuccess(AsyncJob<?, ?, R> asyncJob, R result);

    /**
     * @param asyncJob
     */
    void postCanceled(AsyncJob asyncJob);

    /**
     * @param asyncJob
     * @param msg
     * @param throwable
     */
    void postFailed(AsyncJob asyncJob, String msg, Throwable throwable);

    /**
     *
     */
    <T> void postIntermediate(AsyncJob<?, T, ?> asyncJob, T intermediate);
}
