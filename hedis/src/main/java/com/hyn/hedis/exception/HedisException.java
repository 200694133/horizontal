package com.hyn.hedis.exception;

import java.io.Serializable;

/**
 * Created by hanyanan on 2015/2/16.
 */
public class HedisException extends Exception {
    public HedisException() {
        super();
    }

    public HedisException(String message) {
        super(message);
    }

    public HedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public HedisException(Throwable cause) {
        super(cause);
    }
}
