package hyn.com.storage;

/**
 * Created by hanyanan on 2015/2/13.
 */
public class IllegalKeyException extends Exception{
    public IllegalKeyException() {
        super();
    }

    public IllegalKeyException(String detailMessage) {
        super(detailMessage);
    }


    public IllegalKeyException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }


    public IllegalKeyException(Throwable throwable) {
        super(throwable);
    }
}
