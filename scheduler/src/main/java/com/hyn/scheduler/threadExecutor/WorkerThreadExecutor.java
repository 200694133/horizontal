package com.hyn.scheduler.threadExecutor;

import com.hyn.scheduler.Request;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;

/**
 * Created by hanyanan on 2015/6/3.
 *
 * Provides a thread for performing network dispatch from a queue of requests.
 */
public class WorkerThreadExecutor extends Thread{
    /** The queue of requests to service. */
    @NotNull private final BlockingQueue<Request> mQueue;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;








    public void run(){

    }
}
