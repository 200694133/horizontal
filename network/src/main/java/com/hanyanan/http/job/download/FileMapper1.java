package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hanyanan on 2015/6/22.
 */
public class FileMapper1 {
    public static final String LOG_TAG = "FileMapper1";
    /**
     * Current file tag to identity current map attribute.
     */
    public final int tag;

    private final long length;
    /**
     * 没有被覆盖的区域,按照区域大小排序
     */
    private final SortedMap<FileRange, FileRange> blockHoleLengthList = Collections.synchronizedSortedMap(new TreeMap<FileRange, FileRange>(LENGTH_COMPARATOR));

    /**
     * 没有被覆盖的区域,按照区域起始位置排序
     */
    private final SortedMap<FileRange, FileRange> blockHoleOffsetList = Collections.synchronizedSortedMap(new TreeMap<FileRange, FileRange>(POSITION_COMPARATOR));


    public FileMapper1(long totalLength) {
        this.length = totalLength;
        tag = new Random().nextInt();
        FileRange range = new FileRange(tag, 0, totalLength);
        blockHoleLengthList.put(range, range);
        blockHoleOffsetList.put(range, range);
    }


    protected synchronized void onFinish(long offset, long length) {
        // TODO
    }

    protected synchronized boolean storeFinish(long offset, long length) {
        // TODO
        return true;
    }

    public synchronized void finish(long offset, long length, boolean needStore) {
        /*
        * 第一步，找到映射重叠区
        * */
        List<FileRange> conflictRange = findFlictRect(offset, length);
        if (null == conflictRange) {
            /*
            * 没用冲突，表明当前是无用的，即重复下载了, 直接抛弃当前信息
            * */
            HttpLog.w(LOG_TAG, "repeat download: From " + offset + " To " + (offset + length - 1));
            return;
        }
        /*
        * 第二步，保存完成信息
        * */
        if (needStore && !storeFinish(offset, length)) {
            /*
            * 保存信息失败, 重新下载
            * */
            HttpLog.w(LOG_TAG, "store idx failed: From " + offset + " To " + (offset + length - 1));
            return;
        }

        for (FileRange range : conflictRange) {
            if(range.locked) {
                /*
                * 被锁定，存在重复下载
                * */
                HttpLog.w(LOG_TAG, "Repeat download from range: " + range);
                continue;
            }
            if (range.offset >= offset && range.offset + range.length - 1 <= (offset + length - 1)) {
                /*
                * 完全被包含在指定的区域内
                * */
                blockHoleOffsetList.remove(range);
                blockHoleLengthList.remove(range);
                HttpLog.d(LOG_TAG, "download success range: " + range);
                continue;
            }

            /*
             * 左边部分被覆盖
             */

        }
    }

    public synchronized void finish(FileRange range) {
        if (range.tag != tag) {
            throw new IllegalArgumentException("Current rang is not delivery from current provider!");
        }
        /*
        * 恢复当前锁，使之可以重用
        * */
        range.locked = false;
        /*
        * 保存下载索引信息
        * */
        if (!storeFinish(range.offset, range.length)) {
            /*
            * 保存信息失败, 重新下载
            * */
            return;
        }
        /*
        * 当前区域下载成功，删除节点
        * */
        blockHoleLengthList.remove(range);
        blockHoleOffsetList.remove(range);
    }

    public synchronized void abort(FileRange range) {
        if (range.tag != tag) {
            throw new IllegalArgumentException("Current rang is not delivery from current provider!");
        }
        range.locked = false;
    }

    public synchronized void partlyDone(FileRange range, long partlyLength) {
        // TODO
    }

    public synchronized FileRange delivery(long length) {

        return null;
    }

    /**
     * 找到重叠区，如果没有重叠，则返回当前区域，否则返回重叠区。
     *
     * @param offset
     * @param length
     * @return
     */
    private synchronized List<FileRange> findFlictRect(long offset, long length) {
        Collection<FileRange> fileRanges = blockHoleOffsetList.values();
        if (null == fileRanges || fileRanges.isEmpty()) {
            return null;
        }
        List<FileRange> fileRangeList = new LinkedList<FileRange>(fileRanges);
        final List<FileRange> res = new LinkedList<FileRange>();
        FileRange prev;
        for (int index = 0; index < fileRangeList.size() - 1; ++index) {
            FileRange range = fileRangeList.get(index);
            if (range.offset < offset || range.offset + range.length < offset) {
                continue;
            }
            if (range.offset > offset + length) {
                break;
            }
            res.add(range);
        }

        return res;
    }


    public synchronized void merge() {
        Collection<FileRange> fileRanges = blockHoleOffsetList.values();
        if (null == fileRanges || fileRanges.isEmpty()) {
            return;
        }
        List<FileRange> fileRangeList = new LinkedList<FileRange>(fileRanges);

        for (int index = 0; index < fileRangeList.size() - 1; ) {
            FileRange r1 = fileRangeList.get(index);
            if (r1.locked) {
                ++index;
                continue;
            }
            FileRange r2 = fileRangeList.get(index + 1);
            if (r1.length + r1.offset >= r2.offset) {
                // 合并两个模块
                r1.length += r2.length;
                fileRangeList.remove(index + 1);
                blockHoleOffsetList.remove(r2);
                blockHoleLengthList.remove(r2);
                continue;
            }
            ++index;
        }
    }

    /**
     * 起始位置排序
     */
    private static final Comparator<FileRange> POSITION_COMPARATOR = new Comparator<FileRange>() {
        @Override
        public int compare(FileRange o1, FileRange o2) {
            if (o1.offset > o2.offset) {
                return 1;
            }
            if (o1.offset == o2.offset) {
                return 0;
            }
            return -1;
        }
    };

    /**
     * 大小从小到大排序
     */
    private static final Comparator<FileRange> LENGTH_COMPARATOR = new Comparator<FileRange>() {
        @Override
        public int compare(FileRange o1, FileRange o2) {
            if (o1.length > o2.length) {
                return 1;
            }
            if (o1.length == o2.length) {
                return 0;
            }
            return -1;
        }
    };

    static class FileRange {
        private FileRange(int tag, long offset, long length) {
            this.tag = tag;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public int hashCode() {
            return tag + Long.valueOf(offset).hashCode() + Long.valueOf(length).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (FileRange.class.isInstance(obj)) {
                FileRange range = (FileRange) obj;
                return range.length == length && range.offset == offset && range.tag == tag;
            }
            return false;
        }

        @Override
        public String toString() {
            return "From " + offset + "\tTo " + (length + offset - 1);
        }

        public int tag;
        public long offset;
        public long length;
        private boolean locked;
    }
}
