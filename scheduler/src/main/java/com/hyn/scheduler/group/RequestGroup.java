package com.hyn.scheduler.group;

import com.hyn.scheduler.Request;
import com.hyn.scheduler.RequestLoader;
import com.hyn.scheduler.RunningStatus;

import java.util.List;

/**
 * Created by hanyanan on 2015/5/31.
 */
public interface RequestGroup{
    List<Request> getChildren();

    void add(Request request);

    int getCount();

    Request indexOf(int index);

    void cancel();

    Request remove(int index);

    void remove(Request request);

    RunningStatus getRunningStatus();

    RequestLoader getRequestLoader();

    void dispatchRequest();
}
