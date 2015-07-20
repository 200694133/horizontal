package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/18.
 */
public class UnRetryException extends Exception implements UnRetryable {
    public UnRetryException(){
        super();
    }
    public UnRetryException(String message) {
        super(message);
    }
    public UnRetryException(Throwable cause) {
        super(cause);
    }
    public UnRetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
