package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import hyn.com.lib.IOUtil;
import hyn.com.lib.Preconditions;

/**
 * Created by hanyanan on 2015/6/16.
 */
public class DiscreteVirtualFileDescriptorProvider implements VirtualFileDescriptorProvider {
    /**
     * create a fileMapper to manege the file hole state.
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

    private final RandomAccessFile randomAccessDestFile;
    /**
     * The dest file will store the data.
     */
    private final File file;
    /**
     * The config file to store the dwonload information of {@link #file} which real data stored..
     */
    private final File configFile;

    private boolean isClosed = false;
    public DiscreteVirtualFileDescriptorProvider(File dstFile, long size, long blockSize) throws IOException {
        this.blockSize = blockSize;
        this.fileSize = size;
        this.file = dstFile;
        randomAccessDestFile = new RandomAccessFile(dstFile, "rw");
        randomAccessDestFile.setLength(size);

        configFile = new File(file.getAbsolutePath() + ".config");
        if (null == configFile || !configFile.exists()) {
            this.fileMapper = FileMapper.create(fileSize);
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(configFile);
        byte[] data = IOUtil.getBytesFromStream(fileInputStream);
        this.fileMapper = FileMapper.create(blockSize, data);
        IOUtil.closeQuietly(fileInputStream);
    }

    private String getLogName() {
        return file.getAbsolutePath();
    }

    @Override
    public VirtualFileDescriptor deliveryAndLock() {
        Preconditions.checkState(!isClosed, "Cannot delivery descriptor from closed provider!");
        HttpLog.d("Http", "deliveryAndLock try delivery " + blockSize + " length");
        Range range = fileMapper.deliveryRange(blockSize);
        HttpLog.d("Http", "deliveryAndLock range " + range);
        return create(range);
    }

    /**
     * Store current progress to disk.
     */
    @Override
    public void backup() {
        byte[] config = fileMapper.store();
        FileOutputStream fileInputStream = null;
        try {
            fileInputStream = new FileOutputStream(configFile);
            fileInputStream.write(config);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeQuietly(fileInputStream);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            backup();
            isClosed = true;
            IOUtil.closeQuietly(randomAccessDestFile);
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            byte[] config = fileMapper.store();
            // TODO
            IOUtil.closeQuietly(randomAccessDestFile);
        }
    }



    public void onFragmentComplete(long length){
        backup();
    }


    private VirtualFileDescriptor create(Range rage) {
        VirtualFileDescriptor descriptor = new AbstractVirtualFileDescriptor(this, rage) {
            @Override
            public boolean isClosed() {
                if(super.isClosed() || isClosed) {
                    return true;
                }
                return false;
            }

            @Override
            public void write(byte[] buff, int offset, int length) throws IOException {
                synchronized (DiscreteVirtualFileDescriptorProvider.this) {
                    synchronized (this) {
                        if(isClosed()) {
                            return ;
                        }
                        Preconditions.checkState(!finished, "The descriptor has finished, cannot write gain.");
                        randomAccessDestFile.seek(range.offset + hasWritten);
                        randomAccessDestFile.write(buff, offset, length);
                        hasWritten += length;
                        HttpLog.d("Http", getLogName() + "\t" + range.toString() + "write hasWritten " + hasWritten);
                    }
                }
            }

            @Override
            public void adjustNewLength(long newLength) throws ResizeConflictException {
                synchronized (this) {
                    if(isClosed()) {
                        return ;
                    }
                    Preconditions.checkState(!finished, "The descriptor has finished, cannot change length yet.");
                    Preconditions.checkState(hasWritten <= 0, "The descriptor has written to the file, cannot change the size.");
                    fileMapper.resize(this.range, newLength);
                    HttpLog.d("Http", getLogName() + "\tadjustNewLength " + newLength);
                }
            }

            @Override
            public void finish() {
                synchronized (this) {
                    if(isClosed()) {
                        return ;
                    }
                    fileMapper.finish(this.range);
                    onFragmentComplete(this.length());
                    super.finish();
                    HttpLog.d("Http", getLogName() + "\tfinish " + range);
                }
            }

            @Override
            public void abort() {
                synchronized (this) {
                    if(isClosed()) {
                        return ;
                    }
                    fileMapper.abort(this.range);
                    super.abort();
                    HttpLog.d("Http", getLogName() + "\tabort " + range);
                }
            }
        };
        return descriptor;
    }
}
