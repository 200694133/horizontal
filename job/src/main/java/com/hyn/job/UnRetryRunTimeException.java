package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/18.
 */
public class UnRetryRunTimeException extends RuntimeException implements UnRetryable {
    public UnRetryRunTimeException(){
        super();
    }
    public UnRetryRunTimeException(String message) {
        super(message);
    }

    public UnRetryRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
