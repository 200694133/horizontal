package com.hyn.hedis.exception;

/**
 * Created by hanyanan on 2015/2/16.
 */
public class IllegalOutPolicyException extends HedisException{
    public IllegalOutPolicyException() {
        super();
    }

    public IllegalOutPolicyException(String message) {
        super(message);
    }
}
