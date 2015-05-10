package hyn.com.datastorage.exception;

/**
 * Created by hanyanan on 2015/3/30.
 */
public class TypeConflictException extends DataException {
    public TypeConflictException() {
        super();
    }

    public TypeConflictException(String message) {
        super(message);
    }

    public TypeConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeConflictException(Throwable cause) {
        super(cause);
    }
}
