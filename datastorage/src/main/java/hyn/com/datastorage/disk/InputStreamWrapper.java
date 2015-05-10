package hyn.com.datastorage.disk;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2014/8/22.
 */
public class InputStreamWrapper extends InputStream implements Abortable {
    private final IStreamStorage.Snapshot mSnapShot;
    private final InputStream mInputStream;
    InputStreamWrapper(IStreamStorage.Snapshot snapShot, InputStream inputStream) {
        mSnapShot = snapShot;
        mInputStream = inputStream;
    }

    //TODO
    public int available() throws IOException { return mInputStream.available(); }

    public void close() throws IOException {
        mInputStream.close();
        mSnapShot.close();
    }

    public void mark(int readlimit) { mInputStream.mark(readlimit); }

    public boolean markSupported() { return mInputStream.markSupported(); }

    public int read() throws IOException{
        return mInputStream.read();
    }

    public int read(byte[] buffer) throws IOException {
        return mInputStream.read(buffer);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        return mInputStream.read(buffer,offset,length);
    }

    public synchronized void reset() throws IOException {
        mInputStream.reset();
    }

    public long skip(long byteCount) throws IOException {
        return mInputStream.skip(byteCount);
    }

    @Override
    public void abort() throws IOException {
        mSnapShot.close();
        mInputStream.close();
    }
}
