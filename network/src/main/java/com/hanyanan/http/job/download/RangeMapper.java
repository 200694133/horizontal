package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpLog;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import hyn.com.lib.Preconditions;

import static hyn.com.lib.Preconditions.checkArgument;

/**
 * Created by hanyanan on 2015/6/22.
 */
public class RangeMapper {
    public static final String LOG_TAG = "RangeMapper";

    /**
     * Current file tag to identity current map attribute.
     */
    public final int tag;

    /**
     * Total file length.
     */
    private final long length;

    /**
     * 没有被覆盖的区域,按照区域大小排序
     */
    private final SortedMap<FileRange, FileRange> blockHoleLengthList = Collections.synchronizedSortedMap(new TreeMap<FileRange, FileRange>(LENGTH_COMPARATOR));

    /**
     * 没有被覆盖的区域,按照区域起始位置排序
     */
    private final SortedMap<FileRange, FileRange> blockHoleOffsetList = Collections.synchronizedSortedMap(new TreeMap<FileRange, FileRange>(POSITION_COMPARATOR));

    /**
     * The hole size
     */
    private long holeSize = 0;

    public RangeMapper(long totalLength) {
        this.length = totalLength;
        this.holeSize = this.length;
        tag = new Random().nextInt();
        FileRange range = new FileRange(tag, 0, totalLength);
        blockHoleLengthList.put(range, range);
        blockHoleOffsetList.put(range, range);
    }

    public synchronized long getHoleSize() {
        return holeSize;
    }


    protected synchronized boolean saveFinishState(long offset, long length) {
        HttpLog.d(LOG_TAG, "saveFinishState From " + offset + " To " + (length + offset - 1));
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
        if (needStore && !saveFinishState(offset, length)) {
            /*
            * 保存信息失败, 重新下载
            * */
            HttpLog.w(LOG_TAG, "store idx failed: From " + offset + " To " + (offset + length - 1));
            return;
        }

        for (final FileRange range : conflictRange) {
            if (range.locked) {
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
                holeSize -= range.length;
                HttpLog.d(LOG_TAG, "download success range: " + range);
                continue;
            }

            /*
             * 部分被覆盖
             */
            long left = offset - range.offset; // 左边的间距
            long right = range.length + range.offset - 1 - (offset + length - 1); // 右边的间距
            blockHoleLengthList.remove(range);
            blockHoleOffsetList.remove(range);
            HttpLog.d(LOG_TAG, "Remove conflict range: " + range);
            if (left > 0) {
                // 右边相交
                FileRange fileRange = new FileRange(tag, range.offset, left);
                blockHoleLengthList.put(fileRange, fileRange);
                blockHoleOffsetList.put(fileRange, fileRange);
                holeSize -= range.length - left;
                HttpLog.d(LOG_TAG, "Add from conflict range: " + fileRange);
            }

            if (right > 0) {
                // 左边相交
                FileRange fileRange = new FileRange(tag, offset + length, right);
                blockHoleLengthList.put(fileRange, fileRange);
                blockHoleOffsetList.put(fileRange, fileRange);
                holeSize -= range.length - right;
                HttpLog.d(LOG_TAG, "Add from conflict range: " + fileRange);
            }
        }
    }

    public synchronized void finish(final FileRange range) {
        checkArgument(range.tag == tag, "Current rang is not delivery from current provider!");
        /*
        * 恢复当前锁，使之可以重用
        * */
        range.locked = false;
        /*
        * 保存下载索引信息
        * */
        if (!saveFinishState(range.offset, range.length)) {
            /*
            * 保存信息失败, 重新下载
            * */
            HttpLog.w(LOG_TAG, "SaveFinishState failed, retry again.");
            return;
        }
        /*
        * 当前区域下载成功，删除节点
        * */
        blockHoleLengthList.remove(range);
        blockHoleOffsetList.remove(range);
        holeSize -= range.length;
        HttpLog.d(LOG_TAG, "Save range success : " + range);
    }

    public synchronized void abort(FileRange range) {
        checkArgument(range.tag == tag, "Current rang is not delivery from current provider!");
        range.locked = false;
        HttpLog.d(LOG_TAG, "Abort range : " + range);
    }

    public synchronized void partlyDone(final FileRange range, long partlyLength) {
        checkArgument(range.tag == tag, "Current rang is not delivery from current provider!");
        range.locked = false;
        /*
        * 保存下载索引信息
        * */
        if (!saveFinishState(range.offset, partlyLength)) {
            /*
            * 保存信息失败, 重新下载
            * */
            HttpLog.w(LOG_TAG, "SaveFinishState failed, retry again.");
            return;
        }

        /*
        * 当前区域部分下载成功，删除节点
        * */
        blockHoleLengthList.remove(range);
        blockHoleOffsetList.remove(range);

        if (partlyLength >= range.length) {
            return;
        }

        range.offset = range.offset + partlyLength;
        range.length = range.length - partlyLength;

        blockHoleLengthList.put(range, range);
        blockHoleOffsetList.put(range, range);
    }

    public synchronized FileRange delivery(long maxLength) {
        Collection<FileRange> fileRanges = blockHoleLengthList.values();
        if (null == fileRanges || fileRanges.isEmpty()) {
            return null;
        }
        List<FileRange> fileRangeList = new LinkedList<FileRange>(fileRanges);
        FileRange res = null;
        int size = fileRangeList.size();
        for (int i = 0; i < size; ++i) {
            FileRange fileRange = fileRangeList.get(i);
            if (fileRange.locked) {
                continue;
            }
            if (fileRange.length <= maxLength) {
                res = fileRange;
                fileRange.locked = true;
                break;
            }
            /**
             * 拆分，将一个大的，拆成两个小的
             */
            FileRange expect = new FileRange(tag, fileRange.offset, maxLength);
            FileRange leave = new FileRange(tag, fileRange.offset + maxLength, fileRange.length - maxLength);
            expect.locked = true;
            res = expect;
            blockHoleLengthList.remove(fileRange);
            blockHoleOffsetList.remove(fileRange);
            blockHoleLengthList.put(expect, expect);
            blockHoleOffsetList.put(expect, expect);
            blockHoleLengthList.put(leave, leave);
            blockHoleOffsetList.put(leave, leave);
            break;
        }

        return res;
    }

    public synchronized FileRange adjustDeliveryedRange(FileRange range, long partlyLength) {
        checkArgument(range.tag == tag && range.locked, "Current rang is not delivery from current provider!");
        if (partlyLength >= range.length) {
            // TODO
            return range;
        }

        blockHoleLengthList.remove(range);
        blockHoleOffsetList.remove(range);

        FileRange range1 = new FileRange(tag, range.offset, partlyLength);
        range1.locked = true;
        blockHoleLengthList.put(range1, range1);
        blockHoleOffsetList.put(range1, range1);

        FileRange range2 = new FileRange(tag, range.offset + partlyLength, range.length - partlyLength);
        blockHoleLengthList.put(range2, range2);
        blockHoleOffsetList.put(range2, range2);
        return range1;
    }

    /**
     * 找到重叠区，如果没有重叠，则返回null，否则返回重叠区。
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
        for (int index = 0; index < fileRangeList.size(); ++index) {
            FileRange range = fileRangeList.get(index);
            if (range.offset >= offset + length || range.offset + range.length - 1 < offset) {
                continue;
            }
            if (range.offset > offset + length) {
                break;
            }
            res.add(range);
        }

        return res;
    }

    /**
     * 合并相邻的区域
     */
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Collection<FileRange> fileRanges = blockHoleOffsetList.values();
        if (null == fileRanges || fileRanges.isEmpty()) {
            return super.toString();
        }
        List<FileRange> fileRangeList = new LinkedList<FileRange>(fileRanges);

        for (int index = 0; index < fileRangeList.size(); ++index) {
            FileRange r1 = fileRangeList.get(index);
            if (r1.locked) {
                sb.append("[locked ");
            } else {
                sb.append("[unlock ");
            }
            sb.append(r1.offset)
                    .append(" , ")
                    .append(r1.offset + r1.length - 1)
                    .append("]\t");
        }
        return sb.toString();
    }

    public static void main(String[] argv) {
        final long length = 1000;
        RangeMapper mapper1 = new RangeMapper(length);
        mapper1.finish(0, 2, true);
        mapper1.finish(7, 20, true);
        mapper1.finish(56, 12, true);
        mapper1.finish(23, 56, true);
        mapper1.finish(98, 12, true);
        mapper1.finish(102, 20, true);
        mapper1.finish(125, 4, true);
        mapper1.finish(189, 100, true);
        mapper1.finish(458, 100, true);
        mapper1.finish(89, 20, true);
        mapper1.finish(526, 30, true);
        mapper1.finish(685, 80, true);
        mapper1.finish(795, 5, true);
        mapper1.finish(500, 47, true);
        mapper1.finish(879, 47, true);
        System.out.println(mapper1.toString());
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
            return "\t[From " + offset + "\tTo " + (length + offset - 1) + "]";
        }

        public int tag;
        public long offset;
        public long length;
        private boolean locked;
    }
}
