package com.hyn.job.processor;

import com.hyn.job.AsyncJob;
import com.hyn.job.JobFunction;

import java.io.InputStream;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/7/17.
 */
public class InputToBytesFunction implements JobFunction<InputStream, byte[]> {
    private final boolean autoClose;
    public InputToBytesFunction(){
        autoClose = false;
    }
    public InputToBytesFunction(boolean autoClose){
        this.autoClose = autoClose;
    }
    @Override
    public byte[] call(AsyncJob asyncJob, InputStream inputStream) throws Throwable {
        byte[] res = IOUtil.inputStreamToBytes(inputStream);
        if(autoClose) {
            IOUtil.closeQuietly(inputStream);
        }
        return res;
    }
}
