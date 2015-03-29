package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import java.util.List;

import hyn.com.lib.ThreeTuple;

/**
 * Created by hanyanan on 2015/3/25.
 */
public interface SetHedis<T> {
    /**
     * Get the default order policy.
     * @return default order policy
     */
    public OrderPolicy getOrderPolicy();

    /**
     * Get Object parser tp change the object to byte[] or change byte[] to object.
     * @return
     */
    public ObjectParser<T> getParser();

    /**
     * Returns the eldest entry in the map, or {@code null} if the map is empty.
     */
    public ThreeTuple<String, String, T> eldest() throws HedisException;
    /**
     * Replace new content to old content if exist, return the old content if exits.
     * @param key the identify key.
     * @param content The body need to put into Hedis.
     * @param expireTimeDelta The time delta that the time point which expired.
     *                        -1 mean that will never expired.
     * @return old content, if not exits then return null
     */
    public void put(final String key, T content, long expireTimeDelta) throws HedisException;
    /**
     * Replace new content to old content if exist, return the old content if exits. It valid
     * forever until the hedis size up to the maximum value.
     * @param key the identify key.
     * @param content The body need to put into Hedis.
     * @return old content, if not exits then return null
     */
    public void put(final String key, T content) throws HedisException;

    /**
     * Replace new content to old content if exist, return the old content if exits.
     * @param key the identify key.
     * @param content The body need to put into Hedis.
     * @param expireTimeDelta The time delta that the time point which expired.
     * @return old content, if not exits then return null
     */
    public T replace(final String key, T content, long expireTimeDelta) throws HedisException;
    /**
     * Replace new content to old content if exist, return the old content if exits. It valid
     * forever until the hedis size up to the maximum value.
     * @param key the identify key.
     * @param content The body need to put into Hedis.
     * @return old content, if not exits then return null
     */
    public T replace(final String key, T content) throws HedisException;

    /**
     * Return  all entries. Do not update any property of the tag. Return key-value pair.
     */
    public List<ThreeTuple<String, String, T>> getAll() throws HedisException;

    /**
     * Get the page of current tag, should update the property attribute. Order by default policy.
     * Return key-value pairs.
     * @param pageIndex
     * @param pageCount
     * @return key-value pairs.
     */
    public List<ThreeTuple<String, String, T>> getPage(int pageIndex, int pageCount) throws HedisException;

    /**
     * Get the specify entry if it's exist.
     * @param key
     * @return Return the specify entry, or {@code null} mean that not exist.
     */
    public ThreeTuple<String, String, T> get(final String key) throws HedisException;

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
    public ThreeTuple<String, String, T> fetch(final String key) throws HedisException;

    //clear all content.
    public void evictAll();

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

    /**
     * Get the tag of current hedis.
     * @return
     */
    public String getTag();
    //if enable expire time restrict
    public boolean enableExpireTime();

    public void setRemoveListener(OnEntryRemovedListener<T> listener);
    //close
    public void dispose();
    /**
     * Called when the out-of date
     * */
    public interface OnEntryRemovedListener<T>{
        //removed listener, this will be called when out max size or max count, the user invoke delete*
        //manual will do not trigger this interface.
        public void onEntryRemoved(String tag, String key, T oldValue);
    }
}
