package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/18.
 */
public class RetryException extends Exception implements Retryable {
    public RetryException(){
        super();
    }
    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
