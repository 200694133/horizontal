package com.hyn.scheduler.impl;

import com.hyn.scheduler.PriorityPolicy;
import com.hyn.scheduler.Request;
import com.hyn.scheduler.RetryPolicy;
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
    public boolean retry(Request request, Throwable throwable) {
        --counter;
        if(counter <= 0) return false;
        return true;
    }

    /**
     * If need change the priority if current
     * @param request
     * @param oldPriority
     * @return
     */
    @Override public PriorityPolicy retryPriority(Request request, @NotNull PriorityPolicy oldPriority) {
        return oldPriority;
    }
}
