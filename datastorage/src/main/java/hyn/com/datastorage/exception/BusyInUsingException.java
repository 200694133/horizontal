package hyn.com.datastorage.exception;

/**
 * Created by hanyanan on 2015/3/30.
 */
public class BusyInUsingException extends Exception {
    public BusyInUsingException() {
        super();
    }

    public BusyInUsingException(String message) {
        super(message);
    }

    public BusyInUsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusyInUsingException(Throwable cause) {
        super(cause);
    }
}