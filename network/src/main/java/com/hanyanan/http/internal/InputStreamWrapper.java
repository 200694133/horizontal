package com.hanyanan.http.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2015/5/12.
 */
public class InputStreamWrapper extends InputStream{
    private final InputStream inputStream;
    private final Closeable closeable;

    public InputStreamWrapper(InputStream inputStream, Closeable closeable) {
        this.inputStream = inputStream;
        this.closeable = closeable;
    }
    @Override
    public int read() throws IOException {
        return 0;
    }
}
