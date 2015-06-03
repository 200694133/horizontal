package com.hyn.scheduler;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/6/2.
 */
public class RequestLoader {
    private static RequestLoader sLoadInstance = null;
    public static synchronized  RequestLoader getInstance(){
        if(null == sLoadInstance) {
            sLoadInstance = new RequestLoader();
        }
        return sLoadInstance;
    }

    private RequestLoader(){

    }

    public void load(Request request){
        //TODO
    }

    public void loadDelayed(Request request, TimeUnit timeUnit, long time) {
        //TODO
    }

    public  void loadDelayed(Request request, long millTimes) {
        //TODO
    }
}
