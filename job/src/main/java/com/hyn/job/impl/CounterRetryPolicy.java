package com.hyn.job.impl;

import com.hyn.job.PriorityPolicy;
import com.hyn.job.AsyncJob;
import com.hyn.job.RetryPolicy;
import org.jetbrains.annotations.NotNull;


/**
 * Created by hanyanan on 2015/6/3.
 */
public class CounterRetryPolicy implements RetryPolicy {
    public static final int DEFAULT_RETRY_COUNT = 3;
    private int counter;
    public CounterRetryPolicy(){
        counter = DEFAULT_RETRY_COUNT;
    }
    public CounterRetryPolicy(int count){
        counter = count;
    }
    @Override
    public boolean retry(AsyncJob asyncJob, Throwable throwable) {
        --counter;
        if(counter <= 0) return false;
        return true;
    }

    /**
     * If need change the priority if current
     * @param asyncJob
     * @param oldPriority
     * @return
     */
    @Override public PriorityPolicy retryPriority(AsyncJob asyncJob, @NotNull PriorityPolicy oldPriority) {
        return oldPriority;
    }
}