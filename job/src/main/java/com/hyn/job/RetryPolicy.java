package com.hyn.job;

import org.jetbrains.annotations.NotNull;

/**
 * Created by hanyanan on 2015/5/31.
 */
public interface RetryPolicy {
    /**
     * If could retry again; {@code true} means support retry again, other with do not retry, delivery failed callback.
     * @param asyncJob
     * @param throwable
     * @return
     */
    public boolean retry(AsyncJob asyncJob, Throwable throwable);

    /***
     * if should change the new priority policy when support retry again.
     * @param asyncJob
     * @return
     */
    public @NotNull PriorityPolicy retryPriority(AsyncJob asyncJob, @NotNull PriorityPolicy oldPriority);
}
