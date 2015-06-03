package com.hyn.scheduler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * Created by hanyanan on 2015/5/31.
 */
public interface RetryPolicy {
    /**
     * If could retry again; {@code true} means support retry again, other with do not retry, delivery failed callback.
     * @param request
     * @param throwable
     * @return
     */
    public boolean retry(Request request, Throwable throwable);

    /***
     * if should change the new priority policy when support retry again.
     * @param request
     * @return
     */
    public @NotNull PriorityPolicy retryPriority(Request request, @NotNull PriorityPolicy oldPriority);
}
