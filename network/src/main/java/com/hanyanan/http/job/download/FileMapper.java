package com.hanyanan.http.job.download;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import hyn.com.lib.Preconditions;

/**
 * Created by hanyanan on 2015/6/16.
 * 用块的方式映射整个文件；
 */
public class FileMapper {
    /**
     * 当于期望的大小相差不超过20%时，认为是可以接受的。
     */
    public static final float ACCEPT_FACTOR = 0.2F;

    /**
     * 适用于大文件的数据块大小，超过4G
     */
    public static final int LARGE_BLOCK_SIZE = 4 * 1024 * 1024;

    /**
     * 适用于文件大小为1G-4G
     */
    public static final int BLOCK_SIZE = 1 * 1024 * 1024;

    /**
     * 使用于小于1G的文件
     */
    public static final int SMALL_BLOCK_SIZE = 256 * 1024;

    /**
     * 没有被覆盖的区域
     */
    private final SortedMap<Range, Range> blockLengthMap = Collections.synchronizedSortedMap(new TreeMap<Range, Range>(LENGTH_COMPARATOR));
    private final long length;

    private FileMapper(long length, List<Range> blockHoleList) {
        Preconditions.checkArgument(length > 0, "Must be large than 1 byte!");
        this.length = length;
        if(null == blockHoleList) {
            Range range = new Range(0, length);
            blockHoleList.add(range);
        }
        Collections.sort(blockHoleList, POSITION_COMPARATOR); // 按起始位置排序
        merge(blockHoleList);
        for (Range r : blockHoleList) {
            blockLengthMap.put(r, r);
        }
    }

    /**
     * 将分散的区域尽可能的连成一片
     */
    private void merge(List<Range> blockHoleList) {
        for (int index = 0; index < blockHoleList.size() - 1; ) {
            Range r1 = blockHoleList.get(index);
            if (r1.deliveryed) {
                continue;
            }
            Range r2 = blockHoleList.get(index + 1);
            if (r1.length + r1.offset == r2.offset) {
                // 合并两个模块
                r1.length += r2.length;
                blockHoleList.remove(index + 1);
            }
        }
    }


    /**
     * 有三种情况：
     * 1.找到期望大小的，则返回当前值；
     * 2.存在比期望大的区域，则从该区域中分配可用的，这会导致存在区域较小的块；
     * 3.所有的区域都比期望的小，则返回最接近期望大小的区域。
     *
     * @param expectLength
     * @return the lock range, if null means that no any range.
     */
    public Range deliveryRange(long expectLength) {
        synchronized (this) {
            Collection<Range> rangeList = blockLengthMap.values();
            long size = rangeList.size();
            Range last = null;
            for (Range range : rangeList) {
                if (range.deliveryed) {
                    continue;
                }
                if (range.length == expectLength) {
                    range.deliveryed = true;
                    return range;
                }
                if (range.length > expectLength) {
                    // 符合情况2， 将一个区域拆分成两个
                    Range o = new Range(range.offset + expectLength, range.length - expectLength);
                    blockLengthMap.put(o, o);
                    range.length = expectLength;
                    range.deliveryed = true;
                    return range;
                }
                last = range;
            }
            if (null == last) {
                // 没有可以分配的range了
                return null;
            }
            // 第三种情况，返回最后面一个区域
            last.deliveryed = true;
            return last;
        }
    }

    /**
     * change the length of specify length, the new length cannot more large the previous length.
     * @param range the specify range
     * @param newLength the length need to change to .
     * @return {@code true} means that resize success, {@code false} means not support resize operation, need
     *  call {@link #abort(Range)} to abort current range.
     */
    public boolean resize(Range range, long newLength) {
        synchronized (this) {
            if(newLength == range.length) {
                // No change, then do nothing
                return true;
            }
            if(newLength < range.length) {
                //change the length of current range
                Range newRange = new Range(range.offset + newLength, range.length - newLength);
                blockLengthMap.put(newRange, newRange);
                range.length = newLength;
                return true;
            }
            return false;
        }
    }

    public void finish(Range range) {
        synchronized (this) {
            if (!blockLengthMap.containsKey(range)) throw new IllegalArgumentException("");
            blockLengthMap.remove(range);
        }
    }

    public void abort(Range range){
        synchronized (this) {
            if (!blockLengthMap.containsKey(range)) throw new IllegalArgumentException("");
            range.deliveryed = false;
        }
    }


    public byte[] store() {
        Store store = new Store();
        synchronized (this) {
            if (blockLengthMap.isEmpty()) return null;
            store.length = this.length;
            store.rangList = new ArrayList<Range>(blockLengthMap.values());
        }
        Gson gson = new Gson();
        return gson.toJson(store).getBytes();
    }

    public static FileMapper create(long length, byte[] data) {
        if (null == data || data.length <= 0) {
            return null;
        }
        Gson gson = new Gson();
        String s = new String(data);
        Store store = gson.fromJson(s, Store.class);
        if (null == store) {
            return null;
        }
        return new FileMapper(store.length, store.rangList);
    }

    public static FileMapper create(long length) {
        return new FileMapper(length, null);
    }

    private static class Store {
        public long length;
        public List<Range> rangList;
    }

    /**
     * 起始位置
     */
    private static final Comparator<Range> POSITION_COMPARATOR = new Comparator<Range>() {
        @Override
        public int compare(Range o1, Range o2) {
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
     * 大小从小到大
     */
    private static final Comparator<Range> LENGTH_COMPARATOR = new Comparator<Range>() {
        @Override
        public int compare(Range o1, Range o2) {
            if (o1.length > o2.length) {
                return 1;
            }
            if (o1.length == o2.length) {
                return 0;
            }
            return -1;
        }
    };
}
