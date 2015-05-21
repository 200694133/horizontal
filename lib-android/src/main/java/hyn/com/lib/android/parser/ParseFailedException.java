package hyn.com.lib.android.parser;

/**
 * Created by hanyanan on 2015/5/21.
 */
public class ParseFailedException extends Exception {
    public ParseFailedException() {
    }

    public ParseFailedException(String detailMessage) {
        super(detailMessage);
    }

    public ParseFailedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ParseFailedException(Throwable throwable) {
        super(throwable);
    }
}
