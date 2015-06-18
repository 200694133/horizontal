package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/18.
 */
public class RetryRunTimeException extends RuntimeException implements UnRetryable {
    public RetryRunTimeException(){
        super();
    }
    public RetryRunTimeException(String message) {
        super(message);
    }
}
