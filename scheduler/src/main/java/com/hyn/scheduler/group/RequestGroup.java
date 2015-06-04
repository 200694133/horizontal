package com.hyn.scheduler.group;

import com.hyn.scheduler.CallbackDelivery;
import com.hyn.scheduler.PriorityPolicy;
import com.hyn.scheduler.Request;
import com.hyn.scheduler.RequestCallback;
import com.hyn.scheduler.RequestExecutor;
import com.hyn.scheduler.RequestLoader;
import com.hyn.scheduler.RetryPolicy;
import com.hyn.scheduler.RunningStatus;

import java.util.List;

import hyn.com.lib.Fingerprint;

/**
 * Created by hanyanan on 2015/5/31.
 */
public abstract class RequestGroup extends Request{
    public RequestGroup(Object param, RequestCallback callback, CallbackDelivery callbackDelivery, RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint, RequestExecutor requestExecutor) {
        super(param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint, requestExecutor);
    }

    abstract List<Request> getChildren();

    abstract void add(Request request);

    abstract int getCount();

    abstract Request indexOf(int index);

    abstract void cancel();

    abstract Request remove(int index);

    abstract void remove(Request request);

    abstract RunningStatus getRunningStatus();

    abstract RequestLoader getRequestLoader();

    abstract void dispatchRequest();
}
