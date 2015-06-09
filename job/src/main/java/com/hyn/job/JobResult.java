package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/9.
 */
public class JobResult<R> {
    private final R response;
    private final String errorMsg;
    private final Throwable throwable;

    JobResult(R response, String msg, Throwable throwable){
        this.response = response;
        this.errorMsg = msg;
        this.throwable = throwable;
    }

    JobResult(R response){
        this(response, null, null);
    }

    JobResult(String msg, Throwable throwable){
        this(null, msg, throwable);
    }

    public R getResponse(){
        return response;
    }

    public String getErrorMsg(){
        return errorMsg;
    }

    public Throwable getThrowable(){
        return throwable;
    }
}
