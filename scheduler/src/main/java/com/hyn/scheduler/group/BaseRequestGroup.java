package com.hyn.scheduler.group;

import com.hyn.scheduler.Request;
import com.hyn.scheduler.RequestLoader;
import com.hyn.scheduler.RunningStatus;
import com.sun.deploy.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hyn.com.lib.binaryresource.ListUtil;

/**
 * Created by hanyanan on 2015/6/2.
 */
abstract class BaseRequestGroup implements RequestGroup{
    protected final List<Request> requestList = new ArrayList<>();
    @Override
    public List<Request> getChildren() {
        return Collections.unmodifiableList(requestList);
    }

    @Override
    public void add(Request request) {
        requestList.add(request);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Request indexOf(int index) {
        return null;
    }

    @Override
    public void cancel() {

    }

    @Override
    public Request remove(int index) {
        return null;
    }

    @Override
    public void remove(Request request) {

    }

    @Override
    public RunningStatus getRunningStatus() {
        return null;
    }

    @Override
    public RequestLoader getRequestLoader() {
        return null;
    }
}
