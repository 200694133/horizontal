package com.hyn.hedis;

/**
 * Created by hanyanan on 2015/2/27.
 */
public enum OrderPolicy {
    DEFAULT,//默认情况，按照元素优先级进行排序，是默认的选项
    LRU,//按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾
    TTL,//按照超时时间排序，最先超时的放在末尾，最后超时的放在头部
    REVERT_TTL,//最先超时的放在头部，最后超时的放在末尾
    Size,//按照从小到大，
    REVERT_SIZE,//按照从大到小
}
