package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

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

    /**
     * Dest file.
     */
    private final RandomAccessFile randomAccessDestFile;
    /**
     * The dest file will store the data.
     */
    private final File destFile;
    /**
     * The config file to store the download information of {@link #destFile} which real data stored..
     */
//    private final File destConfigFile;

    private boolean isClosed = false;

    private final FileWriter configWriter;

    private long currentFinish = 0;

    public RandomFileDescriptorProvider(File dstFile, long size, long blockSize) throws IOException {
        this.blockSize = blockSize;
        this.fileSize = size;
        this.destFile = dstFile;
        randomAccessDestFile = new RandomAccessFile(dstFile, "rw");
        randomAccessDestFile.setLength(size);
        this.rangeMapper = new RangeMapper(fileSize) {
            @Override
            protected synchronized boolean saveFinishState(long offset, long length) {
                try {
                    RandomFileDescriptorProvider.this.saveFinishState(offset, length);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        File destConfigFile = new File(dstFile.getAbsolutePath() + ".config");
//        if (!destConfigFile.exists()) {
//            destConfigFile.createNewFile();
//        }
        initRangeMap(destConfigFile);


        configWriter = new FileWriter(destConfigFile);
    }

    private void initRangeMap(File destConfigFile) {
        FileReader reader = null;
        BufferedReader br = null;
        Map<Long, Long> rangeMaps = new LinkedHashMap<Long, Long>();
        try {
            reader = new FileReader(destConfigFile);
            br = new BufferedReader(reader);
            String s1 = null;
            while ((s1 = br.readLine()) != null) {
                if (ValueUtil.isEmpty(s1)) {
                    continue;
                }
                String[] strings = s1.split("-");
                if (null == strings || strings.length != 2 || ValueUtil.isEmpty(strings[0]) || ValueUtil.isEmpty(strings[1])) {
                    continue;
                }
                long p = Long.parseLong(strings[0]);
                long l = Long.parseLong(strings[1]);
                rangeMaps.put(p, l);
            }

            for (Map.Entry<Long, Long> entry : rangeMaps.entrySet()) {
                rangeMapper.finish(entry.getKey(), entry.getValue(), false);
                currentFinish += entry.getValue() - entry.getKey() + 1;
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

    /**
     * 存储当前已下载的位置
     *
     * @param offset
     * @param length
     * @throws IOException
     */
    private void saveFinishState(long offset, long length) throws IOException {
        synchronized (this) {
            if (isClosed) {
                throw new IOException("Cannot saveFinishState after closed!");
            }
            configWriter.write(offset + "-" + length+"\r\n");
            configWriter.flush();
        }
    }

    private String getLogName() {
        return destFile.getAbsolutePath();
    }

    @Override
    public VirtualFileDescriptor deliveryAndLock() {
        Preconditions.checkState(!isClosed, "Cannot delivery descriptor from closed provider!");
        HttpLog.d("Http", "deliveryAndLock try delivery " + blockSize + " length");
        RangeMapper.FileRange range = rangeMapper.delivery(blockSize);
        if (null == range) {
            return null;
        }
        HttpLog.d("Http", "deliveryAndLock range " + range);
        return create(range);
    }

    @Override
    public synchronized void close() {
        synchronized (this) {
            isClosed = true;
            IOUtil.closeQuietly(randomAccessDestFile);
            IOUtil.closeQuietly(configWriter);
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return isClosed;
    }

    private synchronized void finish(RangeMapper.FileRange range) {
        synchronized (this) {
            rangeMapper.finish(range);
        }
    }

    /**
     * 尝试记录当前已下载的位置
     *
     * @param range
     * @param partlyLength
     */
    private void unSureSaveState(final RangeMapper.FileRange range, long partlyLength) {
        synchronized (this) {
            if (partlyLength <= 0) {
                return;
            }
            try {
                saveFinishState(range.offset, partlyLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void abort(RangeMapper.FileRange range, long currentFinish) {
        synchronized (this) {
            if (currentFinish <= 0) {
                rangeMapper.abort(range);
                return;
            }
            rangeMapper.partlyDone(range, currentFinish);
        }
    }

    private void write(byte[] buff, long fileOffset, int offset, int length) throws IOException {
        synchronized (this) {
            randomAccessDestFile.seek(fileOffset);
            randomAccessDestFile.write(buff, offset, length);
        }
    }

    private VirtualFileDescriptor create(RangeMapper.FileRange rage) {
        VirtualFileDescriptor descriptor = new AbstractVirtualFileDescriptor(this, rage) {
            @Override
            public boolean isClosed() {
                if (super.isClosed() || isClosed) {
                    return true;
                }
                return false;
            }

            @Override
            public void write(byte[] buff, int offset, int length) throws IOException {
                synchronized (this) {
                    if (isClosed()) {
                        throw new IOException("write failed! Current Descriptor has closed!");
                    }
                    RandomFileDescriptorProvider.this.write(buff, range.offset + hasWritten, offset, length);
                    hasWritten += length;
                    HttpLog.d("Http", getLogName() + "\t" + range.toString() + "write hasWritten " + hasWritten);
                }
            }

            @Override
            public void adjustNewLength(long newLength) {
                synchronized (this) {
                    Preconditions.checkState(hasWritten <= 0, "The descriptor has written to the file, cannot change the size.");
                    rangeMapper.adjustDeliveryedRange(this.range, newLength);
                    HttpLog.d("Http", getLogName() + range.toString() + "\tadjustNewLength " + newLength);
                }
            }

            @Override
            public void finish() throws IOException {
                synchronized (this) {
                    if (isClosed()) {
                        throw new IOException("finish failed! Current Descriptor has closed!");
                    }
                    RandomFileDescriptorProvider.this.finish(this.range);
                    super.finish();
                    HttpLog.d("Http", getLogName() + this.range.toString() + "\tfinish ");
                }
            }

            @Override
            public void saveCurrentState() {
                synchronized (this) {
                    if (isClosed()) {
                        HttpLog.w("Http", "saveCurrentState failed! Call saveCurrentState after descriptor has closed!");
                        return;
                    }
                    RandomFileDescriptorProvider.this.unSureSaveState(this.range, hasWritten);
                    HttpLog.d("Http", getLogName() + this.range.toString() + "\tsaveCurrentState " + range + " current state " + hasWritten);
                }
            }


            @Override
            public void abort() {
                synchronized (this) {
                    RandomFileDescriptorProvider.this.abort(this.range, this.hasWritten);
                    super.abort();
                    HttpLog.d("Http", getLogName() + this.range.toString() + "\tabort ");
                }
            }
        };
        return descriptor;
    }
}
