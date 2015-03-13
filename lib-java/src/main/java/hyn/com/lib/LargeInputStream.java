package hyn.com.lib;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2015/2/12.
 * Support large size for input stream.
 */
public abstract class LargeInputStream extends InputStream {
    @Override
    public int available() throws IOException {
        return super.available();
    }

    /**
     * get available length of the stream, it's support large size which more than 2G.
     * @return the available length.
     */
    public abstract long length();

    /**
     * Reset the position to zero. if not support this function than throw exception.
     * @throws java.io.IOException
     */
    public abstract void reset() throws IOException;
}
