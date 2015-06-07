package com.hyn.scheduler.group;

import com.hyn.scheduler.Request;
import com.hyn.scheduler.RequestLoader;

import java.util.Collections;
import java.util.List;

/**
 * Created by hanyanan on 2015/6/2.
 */
public class ConcurrentRequestGroupExecutor extends RequestGroupExecutor<Void> {
    @Override
    public Void performRequest(RequestGroup request) throws Throwable {
        List<Request> requestList = Collections.unmodifiableList(request.getChildren());
        RequestLoader requestLoader = request.requestLoader;
        if(null == requestList) {
            return null;
        }
        if(null == requestLoader) {
            throw new IllegalArgumentException("");
        }

        for(Request req : requestList){
            requestLoader.load(req);
        }
        return null;
    }
}
