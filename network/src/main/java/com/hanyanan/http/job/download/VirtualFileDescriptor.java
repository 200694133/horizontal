package com.hanyanan.http.job.download;

import java.io.File;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2015/6/16.
 * 虚拟的文件描述
 */
public interface VirtualFileDescriptor {
    VirtualFileDescriptorProvider from();

    long length();

    OutputStream getWriterStream();

    long offset();

    void adjustNewLength(long newLength);
}
