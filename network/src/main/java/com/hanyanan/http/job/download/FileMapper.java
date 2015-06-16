package com.hanyanan.http.job.download;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
     * */
    private final List<Range> blockHoleList = new LinkedList<Range>();
    private final SortedMap<Long, Range> blockLengthMap = Collections.synchronizedSortedMap(new TreeMap<Long, Range>());
    private final long length;
    private final long blockSize;
    public FileMapper(long length, long blockSize){
        Preconditions.checkArgument(length > 0, "Must be large than 1 byte!");
        this.length = length;
        this.blockSize = blockSize;
        Range range = new Range(0, length);
        blockHoleList.add(range);
        blockLengthMap.put(length, range);
        Collections.sort(blockHoleList, POSITION_COMPARATOR); // 按起始位置排序
        merge();
    }

    /**
     * 将分散的区域尽可能的连成一片
     */
    private void merge(){
        for(int index = 0; index < blockHoleList.size() - 1;){
            Range r1 = blockHoleList.get(index);
            Range r2 = blockHoleList.get(index + 1);
            if(r1.length + r1.offset == r2.offset) {
                // 合并两个模块
                r1.length += r2.length;
                blockHoleList.remove(index + 1);
            }
        }
    }

    public Range deliveryRange(long expectLength){
        long blockSize = this.blockSize;
        synchronized (this) {
            return null;
        }
    }

    /**
     * 有三种情况：
     * 1.找到期望大小的，则返回当前值；
     * 2.存在比期望大的区域，则从该区域中分配可用的，这会导致存在区域较小的块；
     * 3.所有的区域都比期望的小，则返回最接近期望大小的区域。
     * @param expectLength
     * @return
     */
    private Range findBest(long expectLength){
        Iterator iter = blockLengthMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Long,Range> entry = (Map.Entry<Long,Range>)iter.next();
            if(entry.getKey().longValue() == expectLength) {

            }
        }
        return null;
    }


    public void finish(Range range){

    }

    public void recover(byte[] data){

    }

    public byte[] store(){
        return null;
    }

    /**
     * 起始位置
     */
    private static final Comparator<Range> POSITION_COMPARATOR = new Comparator<Range>(){
        @Override
        public int compare(Range o1, Range o2) {
            if(o1.offset > o2.offset){
                return 1;
            }
            if(o1.offset == o2.offset) {
                return 0;
            }
            return -1;
        }
    };

    /**
     * 大小从小到大
     */
    private static final Comparator<Range> LENGTH_COMPARATOR = new Comparator<Range>(){
        @Override
        public int compare(Range o1, Range o2) {
            if(o1.length > o2.length){
                return 1;
            }
            if(o1.length == o2.length) {
                return 0;
            }
            return -1;
        }
    };
}
