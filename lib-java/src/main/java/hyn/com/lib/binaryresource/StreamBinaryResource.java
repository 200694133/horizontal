package hyn.com.lib.binaryresource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2015/5/26.
 */
public class StreamBinaryResource implements BinaryResource {
    private final InputStream inputStream;
    private final long size;
    private byte[] data;
    public StreamBinaryResource(InputStream stream, long size){
        this.inputStream = stream;
        this.size = size;
    }
    @Override
    public InputStream openStream() throws IOException {
        return inputStream;
    }

    @Override
    public byte[] read() throws IOException {
        return new byte[0];
    }

    @Override
    public long size() {
        return size;
    }
}
