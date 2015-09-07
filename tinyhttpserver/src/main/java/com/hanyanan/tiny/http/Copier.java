package com.hanyanan.tiny.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/12/31. * Copy data from input stream to output stream,user can apply
 * the traffic monitor or encode the data to make it more safe. It's a extendable interface.
 */
public interface Copier {
    /**
     * the core of copy data to output stream.
     * @param inputStream the input stream
     * @param outputStream write the data to this stream
     * @param size size of need to copy, or at the terminal
     * @param encode if need to encode/compress the data
     */
    public void copy(InputStream inputStream, OutputStream outputStream, long size, String encode) throws IOException;
}
