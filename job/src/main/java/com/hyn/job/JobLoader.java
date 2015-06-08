package com.hyn.job;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/6/2.
 */
public class JobLoader {
    private static JobLoader sLoadInstance = null;
    public static synchronized JobLoader getInstance(){
        if(null == sLoadInstance) {
            sLoadInstance = new JobLoader();
        }
        return sLoadInstance;
    }

    private JobLoader(){

    }

    public void load(AsyncJob asyncJob){
        //TODO
    }

    public void loadDelayed(AsyncJob asyncJob, TimeUnit timeUnit, long time) {
        //TODO
    }

    public  void loadDelayed(AsyncJob asyncJob, long millTimes) {
        //TODO
    }

    public void loadSchedule(AsyncJob asyncJob, TimeUnit timeUnit, long timeInterval, int count){
        //TODO
    }

    public void loadSchedule(AsyncJob asyncJob, long timeInterval, int count) {

    }
}
