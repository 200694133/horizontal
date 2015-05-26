package com.hanyanan.http.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * Created by hanyanan on 2015/5/12.
 */
public class InputStreamWrapper extends InputStream{
    private final InputStream inputStream;
    private final HttpURLConnection connection;

    public InputStreamWrapper(InputStream inputStream, HttpURLConnection connection) {
        this.inputStream = inputStream;
        this.connection = connection;
    }
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    public int read(byte b[]) throws IOException {
        return inputStream.read(b);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public void close() throws IOException {
        inputStream.close();
        connection.disconnect();
    }
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
