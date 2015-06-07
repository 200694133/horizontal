package com.hyn.scheduler.group;

import com.hyn.scheduler.Request;

/**
 * Created by n550 on 2015/6/7.
 */
public class ConcurrentPendingRequestGroupExecutor extends RequestGroupExecutor {

    public Request getPendingRequest(){
        return null;
    }
    @Override
    public Object performRequest(RequestGroup request) throws Throwable {
        return null;
    }

    public void onRequestSuccess(){

    }
}
