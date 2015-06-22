package com.hanyanan.http.job.download;

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
    /**
     *
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


    public FileMapper1(long totalLength){
        this.length = totalLength;
        tag = new Random().nextInt();
        FileRange range = new FileRange(tag, 0, totalLength);
        blockHoleLengthList.put(range, range);
        blockHoleOffsetList.put(range, range);
    }


    protected synchronized void onFinish(long offset, long length){

    }

    public synchronized void finish(FileRange range){

    }

    public synchronized void abort(FileRange range){

    }

    public synchronized void partlyDone(FileRange range, long partlyLength){
        // TODO
    }

    public synchronized FileRange delivery(long length){

        return null;
    }

    public synchronized void merge(){
        Collection<FileRange> fileRanges = blockHoleOffsetList.values();
        if(null == fileRanges || fileRanges.isEmpty()) {
            return ;
        }
        List<FileRange> fileRangeList = new LinkedList<FileRange>(fileRanges);

        for (int index = 0 ; index < fileRangeList.size() - 1; ) {
            FileRange r1 = fileRangeList.get(index);
            if (r1.locked) {
                ++ index;
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
            ++ index;
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

    static class FileRange{
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
            if(this == obj) return true;
            if(FileRange.class.isInstance(obj)) {
                FileRange range = (FileRange) obj;
                return range.length==length && range.offset==offset && range.tag == tag;
            }
            return false;
        }

        @Override
        public String toString() {
            return "From "+offset+"\tTo "+(length + offset - 1);
        }

        public int tag;
        public long offset;
        public long length;
        private boolean locked;
    }
}
