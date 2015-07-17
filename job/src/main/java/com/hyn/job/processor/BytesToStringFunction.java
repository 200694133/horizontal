package com.hyn.job.processor;

import com.hyn.job.AsyncJob;
import com.hyn.job.JobFunction;

/**
 * Created by hanyanan on 2015/7/17.
 */
public class BytesToStringFunction implements JobFunction<byte[], String> {
    @Override
    public String call(AsyncJob asyncJob, byte[] bytes) throws Throwable {
        return new String(bytes);
    }
}
