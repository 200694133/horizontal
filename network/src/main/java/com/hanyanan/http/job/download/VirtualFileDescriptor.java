package com.hanyanan.http.job.download;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2015/6/16.
 * 虚拟的文件描述
 */
public interface VirtualFileDescriptor {
    VirtualFileDescriptorProvider from();

    long length();

    void write(byte[] buff, int offset, int length) throws IOException;

    void write(byte[] buff) throws IOException;

    long offset();

    void adjustNewLength(long newLength) throws ResizeConflictException;

    void finish();

    void abort();
}
