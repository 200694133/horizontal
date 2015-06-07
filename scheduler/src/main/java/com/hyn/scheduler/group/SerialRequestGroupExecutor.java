package com.hyn.scheduler.group;

import com.hyn.scheduler.FullPerformer;
import com.hyn.scheduler.Request;
import com.hyn.scheduler.RequestDispatcher;
import com.hyn.scheduler.RequestLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by hanyanan on 2015/6/2.
 */
public class SerialRequestGroupExecutor extends RequestGroupExecutor<Void>{
    FullPerformer fullPerformer;
    @Override
    public Void performRequest(RequestGroup request) throws Throwable {
        List<Request> requestList = new ArrayList<Request>(request.getChildren());
        RequestLoader requestLoader = request.requestLoader;
        if(null == requestList) {
            return null;
        }
        if(null == requestLoader) {
            throw new IllegalArgumentException("");
        }

        final BlockingQueue<Request> blockingQueue = new LinkedBlockingQueue<Request>(requestList);
        fullPerformer = new RequestDispatcher(blockingQueue){
            @Override
            public Request nextRequest() throws InterruptedException {
                return queue.poll();
            }
        };
        fullPerformer.fullPerformRequest();
        return null;
    }
}
