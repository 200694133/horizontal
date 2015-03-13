package com.hyn.hedis.exception;

/**
 * Created by hanyanan on 2015/2/16.
 */
public class IllegalFormatException extends HedisException{
    public IllegalFormatException() {
        super();
    }

    public IllegalFormatException(String message) {
        super(message);
    }
}
