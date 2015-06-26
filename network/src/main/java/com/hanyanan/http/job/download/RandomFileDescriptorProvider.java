package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import hyn.com.lib.IOUtil;
import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/6/16.
 */
public class RandomFileDescriptorProvider implements VirtualFileDescriptorProvider {
    /**
     * create a rangeMapper to manege the file hole state.
     */
    private final RangeMapper rangeMapper;

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
    private final File destFile;
    /**
     * The config file to store the dwonload information of {@link #file} which real data stored..
     */
    private final File destConfigFile;

    private boolean isClosed = false;

    private final FileWriter configWriter;

    public RandomFileDescriptorProvider(File dstFile, long size, long blockSize) throws IOException {
        this.blockSize = blockSize;
        this.fileSize = size;
        this.destFile = dstFile;
        randomAccessDestFile = new RandomAccessFile(dstFile, "rw");
        randomAccessDestFile.setLength(size);
        this.rangeMapper = new RangeMapper(fileSize);

        destConfigFile = new File(dstFile.getAbsolutePath() + ".config");
        if (!destConfigFile.exists()) {
            destConfigFile.createNewFile();
        }
        configWriter = new FileWriter(destConfigFile);

        initRangeMap();
    }

    private void initRangeMap() {
        FileReader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader(destConfigFile);
            br = new BufferedReader(reader);
            String s1 = null;
            while ((s1 = br.readLine()) != null) {
                if(ValueUtil.isEmpty(s1)) {
                    continue;
                }
                String [] strings = s1.split("-");
                if(null == strings || strings.length != 2 || ValueUtil.isEmpty(strings[0]) || ValueUtil.isEmpty(strings[1])) {
                    continue;
                }
                long p = Long.parseLong(strings[0]);
                long l = Long.parseLong(strings[1]);
                rangeMapper.finish(p, l, false);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeQuietly(br);
            IOUtil.closeQuietly(reader);
        }
    }

    private synchronized boolean storeProgress() {
        return true;
    }

    private String getLogName() {
        return destFile.getAbsolutePath();
    }

    @Override
    public VirtualFileDescriptor deliveryAndLock() {
        Preconditions.checkState(!isClosed, "Cannot delivery descriptor from closed provider!");
        HttpLog.d("Http", "deliveryAndLock try delivery " + blockSize + " length");
        RangeMapper.FileRange range = rangeMapper.delivery(blockSize);
        HttpLog.d("Http", "deliveryAndLock range " + range);
        return create(range);
    }

    @Override
    public void close() {
        synchronized (this) {
            isClosed = true;
            IOUtil.closeQuietly(randomAccessDestFile);
            IOUtil.closeQuietly(configWriter);
        }
    }

    private synchronized void finish(RangeMapper.FileRange range){
        synchronized (this) {

        }
    }

    private synchronized void abort(RangeMapper.FileRange range){
        synchronized (this) {

        }
    }

    private void write(byte[] buff, long fileOffset, int offset, int length) throws IOException  {
        synchronized (this) {
            randomAccessDestFile.seek(fileOffset);
            randomAccessDestFile.write(buff, offset, length);
        }
    }

    private VirtualFileDescriptor create(RangeMapper.FileRange rage) {
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
                    synchronized (this) {
                        if(isClosed()) {
                            throw new IOException("Current Descriptor has closed!") ;
                        }
                        RandomFileDescriptorProvider.this.write(buff, range.offset + hasWritten, offset,length);
                        hasWritten += length;
                        HttpLog.d("Http", getLogName() + "\t" + range.toString() + "write hasWritten " + hasWritten);
                    }
            }

            @Override
            public void adjustNewLength(long newLength) {
                synchronized (this) {
                    Preconditions.checkState(hasWritten <= 0, "The descriptor has written to the file, cannot change the size.");
                    rangeMapper.adjustDeliveryedRange(this.range, newLength);
                    HttpLog.d("Http", getLogName() + "\tadjustNewLength " + newLength);
                }
            }

            @Override
            public void finish() throws IOException  {
                synchronized (this) {
                    if(isClosed()) {
                        throw new IOException("Current Descriptor has closed!") ;
                    }
                    RandomFileDescriptorProvider.this.finish(this.range);
                    super.finish();
                    HttpLog.d("Http", getLogName() + "\tfinish " + range);
                }
            }

            @Override
            public void abort() {
                synchronized (this) {
                    RandomFileDescriptorProvider.this.abort(this.range);
                    super.abort();
                    HttpLog.d("Http", getLogName() + "\tabort " + range);
                }
            }
        };
        return descriptor;
    }
}
