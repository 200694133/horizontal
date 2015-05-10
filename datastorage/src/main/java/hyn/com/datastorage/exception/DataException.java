package hyn.com.datastorage.exception;

/**
 * Created by hanyanan on 2015/3/30.
 */
public class DataException extends Exception {
    public DataException() {
        super();
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(Throwable cause) {
        super(cause);
    }
}