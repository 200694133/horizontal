package com.hyn.job;

/**
 * Created by hanyanan on 2015/6/19.
 */
public class DeliveryFailedException extends Exception implements UnRetryable{
    public DeliveryFailedException() {
        super();
    }
    public DeliveryFailedException(String message) {
        super(message);
    }
    public DeliveryFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeliveryFailedException(Throwable cause) {
        super(cause);
    }
    protected DeliveryFailedException(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    private Result tmp;
    public void addTemporary(Result tmp){
        this.tmp = tmp;
    }

    public Result getTmp(){
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
