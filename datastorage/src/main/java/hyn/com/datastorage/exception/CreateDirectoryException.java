package hyn.com.datastorage.exception;

/**
 * Created by hanyanan on 2015/3/30.
 */
public class CreateDirectoryException extends Exception {
    public CreateDirectoryException() {
        super();
    }

    public CreateDirectoryException(String message) {
        super(message);
    }

    public CreateDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateDirectoryException(Throwable cause) {
        super(cause);
    }
}