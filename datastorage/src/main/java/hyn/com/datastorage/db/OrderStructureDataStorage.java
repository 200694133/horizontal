package hyn.com.datastorage.db;

import java.io.InputStream;
import java.util.List;
import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/3/30.
 */
public interface OrderStructureDataStorage<T> extends DeadlineStorage{
    public enum OrderPolicy {
        LRU,//按照最近未访问的顺序排序，最新被访问的放在第一个，最早被访问的放在末尾,最后访问拥有最高的优先级
        Size,//按照从小到大，文件越小优先级越高
        REVERT_SIZE,//按照从大到小，文件越大优先级越高
        TTL,//按照超时时间排序，最先超时的放在末尾，最后超时的放在头部
        REVERT_TTL,//最先超时的放在头部，最后超时的放在末尾，
    }

    /** Get current tag, */
    public String getTag();

    /** Get current order policy */
    public OrderPolicy getOrderPolicy();

    /** Get Object parser tp change the object to byte[] or change byte[] to object.*/
    public ObjectParser<T> getParser();

    public boolean exist(String key);

    /** Returns the eldest entry in the map, or {@code null} if the map is empty. */
    public TwoTuple<String, T> eldest() throws ParseFailedException;

    /**
     * Replace new content to old content if exist, return the old content if exits.
     * @param key the identify key.
     * @param content The body need to put into Hedis.
     * @return old content, if not exits then return null
     */
    public void put(final String key, T content) throws ParseFailedException;

    /**
     * Replace new content to old content if exist, return the old content if exits.
     * @param key the identify key.
     * @return old content, if not exits then return null
     */
    public void put(final String key, InputStream inputStream) throws ParseFailedException;
    /**
     * Replace new content to old content if exist, return the old content if exits.
     * @param key the identify key.
     * @param content The body need to put into Hedis.
     * @return old content, if not exits then return null
     */
    public void put(final String key, T content, Long expireTime) throws ParseFailedException;

    /**
     * Replace new content to old content if exist, return the old content if exits.
     * @param key the identify key.
     * @param inputStream
     *@param expireTime @return old content, if not exits then return null
     */
    public void put(final String key, InputStream inputStream, Long expireTime) throws ParseFailedException;

    /**
     * Return  all entries. Do not update any property of the tag. Return key-value pair.
     */
    public List<TwoTuple<String, T>> getAll() throws ParseFailedException;

    /**
     * Get the page of current tag, should update the property attribute. Order by default policy.
     * Return key-value pairs.
     * @param pageIndex
     * @param pageCount
     * @return key-value pairs.
     */
    public List<TwoTuple<String, T>> getPage(int pageIndex, int pageCount) throws ParseFailedException;

    /**
     * Get the specify entry if it's exist.
     * @param key
     * @return Return the specify entry, or {@code null} mean that not exist.
     */
    public TwoTuple<String, T> get(final String key) throws ParseFailedException;

    /**
     * Get the specify entry if it's exist.
     * @param key
     * @return Return the specify entry, or {@code null} mean that not exist.
     */
    public TwoTuple<String, BinaryResource> getInputStream(final String key);

    /**
     * Remove the specify entry.
     * @param key
     */
    public void remove(final String key);


    /**
     * Return the specify entry and remove it.
     * @param key
     * @return
     */
    public TwoTuple<String, T> fetch(final String key) throws ParseFailedException;

    /**
     * Return the specify entry and remove it.
     * @param key
     * @return
     */
    public TwoTuple<String, BinaryResource> fetchStream(final String key);

    //clear all content.
    public void clear();

    /**
     * Check if contain the key, it need to check if current item out of date, if it true then need
     * delete from both current memory dataBase and disk dataBase.
     * @return current size.
     */
    public int size();

    /**
     * this returns the  maximum sum of the sizes of the entries in this hedis. If do not set
     * the maximum size, then return -1.
     */
    public int maxSize();

    /**
     * This returns the number.
     */
    public int count();

    /**
     * This returns the maximum entries number. May be when user do not set the value.
     */
    public int maxCount();

    /**
     * Limit the max element count, remove illegal element. Delete the lowest order element
     * @param maxRowCount max element count.
     * @return All need removed elements.
     */
    public void trimToCount(int maxRowCount);

    /**
     * @param maxSize the maximum size of the cache before returning. May be -1
     *     to evict even 0-sized elements.
     */
    public void trimToSize(int maxSize);

    public void setRemoveListener(OnOverFlowListener listener);
}
