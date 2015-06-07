package com.hyn.scheduler.group;

import com.hyn.scheduler.Request;
import com.hyn.scheduler.RequestExecutor;
import hyn.com.lib.Preconditions;

/**
 * Created by hanyanan on 2015/6/4.
 */
public abstract class RequestGroupExecutor<R> implements RequestExecutor<R>{

    public abstract R performRequest(RequestGroup request) throws Throwable;

    @Override
    public R performRequest(Request request) throws Throwable {
        Preconditions.checkNotNull(request);
        if(!RequestGroup.class.isInstance(request)) {
            throw new IllegalArgumentException();
        }
        return performRequest((RequestGroup)request);
    }
}
