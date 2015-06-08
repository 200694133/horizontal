package com.hyn.job;

/**
 * Created by hanyanan on 2015/5/31.
 */
public interface JobCallback<I, R> {
    /**
     * Called when finish a request in canceled mode.
     */
    void onCanceled(AsyncJob asyncJob);

    /**
     * Called when a response is received.It's called when get result success.
     */
    void onSuccess(AsyncJob asyncJob, R result);

    /**
     * Callback method that an error has been occurred with the
     * provided error code and optional user-readable message.
     */
    void onFailed(AsyncJob asyncJob, String msg, Throwable throwable);

    /**
     * Called when delivery intermediate data.
     *
     * @param intermediate
     */
    void onIntermediate(I intermediate);
}
