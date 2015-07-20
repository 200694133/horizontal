package com.hanyanan.http.job.download;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2015/6/17.
 */
abstract class AbstractVirtualFileDescriptor implements VirtualFileDescriptor {
    final VirtualFileDescriptorProvider provider;
    final RangeMapper.FileRange range;
    private boolean finished = false;

    /**
     * The length has written to the file.
     */
    long hasWritten = 0;

    AbstractVirtualFileDescriptor(VirtualFileDescriptorProvider provider, RangeMapper.FileRange range){
        this.provider = provider;
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
    public long offset() {
        return range.offset;
    }

    @Override
    public void write(byte[] buff) throws IOException {
        write(buff, 0, buff.length);
    }

    @Override
    public abstract void adjustNewLength(long newLength);

    @Override
    public boolean isClosed() {
        return finished;
    }

    @Override
    public void finish() throws IOException {
        finished = true;
    }

    @Override
    public long finished() {
        return hasWritten;
    }

    @Override
    public void abort() {
        finished = true;
    }
}
