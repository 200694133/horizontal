package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/19.
 */
public class UnexpectedResponseException extends Exception implements UnRetryable{
    public UnexpectedResponseException() {
        super();
    }
    public UnexpectedResponseException(String message) {
        super(message);
    }
    public UnexpectedResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedResponseException(Throwable cause) {
        super(cause);
    }
    protected UnexpectedResponseException(String message, Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    private Result tmp;

    public UnexpectedResponseException unexpectedResponse(Result tmp){
        this.tmp = tmp;
        return this;
    }

    public Result getUnexpectedResponse(){
        return tmp;
    }

    public static class Result<T> {
        private final T response;
        public Result(T response){
            this.response = response;
        }
        public T getValue(){
            return response;
        }
    }
}
