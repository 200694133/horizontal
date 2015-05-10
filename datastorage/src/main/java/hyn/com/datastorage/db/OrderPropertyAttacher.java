package hyn.com.datastorage.db;

import hyn.com.datastorage.db.OrderStructureDataStorage.OrderPolicy;

/**
 * Created by hanyanan on 2015/4/23.
 */
public interface OrderPropertyAttacher {
    /** Synchronized the entry between two database. */
    public void sync(BasicDataBaseHelper helper);
    /**
     * Return the eldest key. Update the last access time property.
     */
    public String eldest(OrderPolicy orderPolicy);

    /** Insert or replace a entry to the current storage.
     * This method will return the delete where selection which include time restriction when out-of-max
     * size/count.
     * */
    public void put(String key, int size, long lastAccessTime, long expireTime);

    /** Insert or replace a entry to the current storage.
     * This method will return the delete where selection which include time restriction when out-of-max
     * size/count.
     * */
    public void put(String key, int size, long expireTime);

    /**
     * Return the order policy.
     * @return
     */
    public String getPage(int pageIndex, int pageCount, OrderPolicy orderPolicy);

    /**
     * Check if exist current request entry, do not update the last access time.
     * @param key the specify entry.
     */
    public boolean exist(String key);
    /**
     * Check if exist current request entry, different from {@link #exist} , this method will update
     * the last access time.
     * @param key the specify entry.
     */
    public boolean get(String key);

    public void remove(String key);

    /** Remove all entries. */
    public void clear();

    /** Return the current size. */
    public int size();

    /** Return the current count of entry. */
    public int count();
    /** Return the max size */
    public int maxSize();

    /** Return the max count */
    public int maxCount();
    /**
     * Return the delete selector.. The selection include time restriction, so please note that do not
     * add the time restriction again.
     * @param maxRowCount
     * @return
     */
    public String trimToCount(int maxRowCount, OrderPolicy orderPolicy);

    /**
     * Return the delete selector..
     * @param maxSize
     * @return
     */
    public String trimToSize(int maxSize, OrderPolicy orderPolicy);


    public void setRemoveListener(OnOverFlowListener listener);

    /** Clear all out-of-date entries.
     * Return true if need cleat trash, other wise do nothing.
     * */
    public boolean clearTrash() ;

    /**
     * Check the expire timeout property, it will ignore the time restriction.
     * @param key
     * @return
     */
    public boolean isExpired(String key);

    public boolean enableExpireRestriction();
}
