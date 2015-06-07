package com.hyn.scheduler.group;

import com.hyn.scheduler.CallbackDelivery;
import com.hyn.scheduler.PriorityPolicy;
import com.hyn.scheduler.Request;
import com.hyn.scheduler.RequestCallback;
import com.hyn.scheduler.RequestExecutor;
import com.hyn.scheduler.RequestLoader;
import com.hyn.scheduler.RetryPolicy;
import com.hyn.scheduler.RunningStatus;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import hyn.com.lib.Fingerprint;
import sun.swing.ImageCache;

/**
 * Created by hanyanan on 2015/5/31.
 */
public class RequestGroup extends Request {
    protected final RequestLoader requestLoader;
    protected final List<Request> requestList = new LinkedList<Request>();
    public RequestGroup(RequestLoader requestLoader, Object param, RequestCallback callback, CallbackDelivery callbackDelivery,
                        RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint,
                        RequestGroupExecutor requestExecutor) {
        super(param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint, requestExecutor);
        this.requestLoader = requestLoader;
    }

    public List<Request> getChildren(){
        return Collections.unmodifiableList(requestList);
    }

    public void add(Request request){
        synchronized (requestList) {
            requestList.add(request);
        }
    }

    public int getCount(){
        return requestList.size();
    }

    public Request indexOf(int index){
        synchronized (requestList) {
            if(index >= requestList.size()) {
                return null;
            }

            return requestList.get(index);
        }
    }


    public Request remove(int index){
        synchronized (requestList) {
            if(index >= requestList.size()){
                return null;
            }

            return requestList.remove(index);
        }
    }



    public void remove(Request request){
        //TODO
    }
}
