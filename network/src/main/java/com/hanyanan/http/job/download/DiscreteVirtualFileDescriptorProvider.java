package com.hanyanan.http.job.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/6/16.
 */
public class DiscreteVirtualFileDescriptorProvider implements VirtualFileDescriptorProvider {
    /**
     * create a filemapper to manege the file hole state.
     */
    private final FileMapper fileMapper;

    /**
     * The expect block size.
     */
    private final long blockSize;
    /**
     * the dest file size(length)
     */
    private final long fileSize;

    private final RandomAccessFile randomAccessFile;
    /**
     * The dest file will store the data.
     */
    private final File file;
    /**
     * The config file to store the dwonload information of {@link #file} which real data stored..
     */
    private final File configFile;
    public DiscreteVirtualFileDescriptorProvider(File dstFile, long size, long blockSize) throws IOException {
        this.blockSize = blockSize;
        this.fileSize = size;
        this.file = dstFile;
        randomAccessFile = new RandomAccessFile(dstFile, "rw");
        randomAccessFile.setLength(size);

        configFile = new File(file.getAbsolutePath()+".config");
        if(null == configFile || !configFile.exists()) {
            this.fileMapper = FileMapper.create(fileSize);
            return ;
        }
        FileInputStream fileInputStream = new FileInputStream(configFile);
        byte[] data = IOUtil.getBytesFromStream(fileInputStream);
        this.fileMapper = FileMapper.create(data);
        IOUtil.closeQuietly(fileInputStream);
    }
=
    @Override
    public VirtualFileDescriptor deliveryAndLock() {
        Range range = fileMapper.
        return null;
    }

    @Override
    public void unlock(VirtualFileDescriptor descriptor) throws InterruptedException {

    }

    @Override
    public void completeAndUnlock(VirtualFileDescriptor descriptor) {

    }

    @Override
    public void failedAndUnlock(VirtualFileDescriptor descriptor) {

    }

    @Override
    public void dispose() {

    }

    private VirtualFileDescriptor create(Range rage, OutputStream outputStream){
        VirtualFileDescriptor descriptor = new AbstractVirtualFileDescriptor(this, outputStream, rage){
            @Override
            public void adjustNewLength(long newLength) throws ResizeConflictException {
                fileMapper.resize(this.range, newLength);
            }

            @Override
            public void finish() {
                fileMapper.finish(this.range);
            }

            @Override
            public void abort() {
                fileMapper.abort(this.range);
            }
        };
        return descriptor;
    }
}
