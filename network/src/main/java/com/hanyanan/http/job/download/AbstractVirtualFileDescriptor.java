package com.hanyanan.http.job.download;

import java.io.OutputStream;

/**
 * Created by hanyanan on 2015/6/17.
 */
abstract class AbstractVirtualFileDescriptor implements VirtualFileDescriptor {
    final VirtualFileDescriptorProvider provider;
    final OutputStream outputStream;
    final Range range;
    AbstractVirtualFileDescriptor(VirtualFileDescriptorProvider provider, OutputStream outputStream, Range range){
        this.provider = provider;
        this.outputStream = outputStream;
        this.range = range;
    }

    @Override
    public VirtualFileDescriptorProvider from() {
        return provider;
    }

    @Override
    public long length() {
        return range.length;
    }

    @Override
    public OutputStream getWriterStream() {
        return outputStream;
    }

    @Override
    public long offset() {
        return range.offset;
    }

    @Override
    public abstract void adjustNewLength(long newLength) throws ResizeConflictException;

    @Override
    public abstract void finish();

    @Override
    public abstract void abort() ;
}
