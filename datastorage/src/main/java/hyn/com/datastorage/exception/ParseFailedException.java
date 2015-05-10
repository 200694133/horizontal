package hyn.com.datastorage.exception;

/**
 * Created by hanyanan on 2015/3/30.
 */
public class ParseFailedException extends DataException {
    public ParseFailedException() {
        super();
    }

    public ParseFailedException(String message) {
        super(message);
    }

    public ParseFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseFailedException(Throwable cause) {
        super(cause);
    }
}
