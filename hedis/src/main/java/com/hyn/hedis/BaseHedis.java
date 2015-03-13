package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by hanyanan on 2015/2/15.
 * A fake redis data base, support base/collection/map structure.
 * the list structure as follow:
 * |------------------------------------------------------------------------  list  ------------------------------------------------------------------------------------|
 * |---- key1 ----|---- key ----|---- priority ----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- 0001 ----|---- list1 --|----1232131321----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- 0002 ----|---- list1 --|----1232122222----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * Column key1 and key2 as te primary key.
 *
 * the set structure as follow:
 * |--------------------------------------------------------------------------  set  ----------------------------------------------------------------------------------------|
 * |---- key -----|---- hashCode ----|---- priority ----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- set1 ----|---- 12312312 ----|----1232131321----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- set1 ----|---- 22222222 ----|----1232122222----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * column key and hashCode as the primary key
 *
 * the map structure as follow:
 * |----------------------------------------------------------  map  ------------------------------------------------------------------|
 * |---- key -----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- map1 ----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- map2 ----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |-----------------------------------------------------------------------------------------------------------------------------------|
 * column key as the primary key
 * .
 *  </p>
 * User can apply list, hashMap, set.
 * More priority means that it's has low
 * If you want to delete a specify entry, you must
 */
public interface BaseHedis {
    /**
     * Check if exits current key entry.
     * @param key
     * @return true has exits, other wise
     */
    public boolean exits(final String key);

    /**
     * Get the count of the entries.
     * @param key
     * @return the sum count of current key.
     */
    public int count(final String key);

    /**
     * Returns whether the stack is empty or not.
     *
     * @return {@code true} if the stack is empty, {@code false} otherwise.
     */
    public boolean empty(final String key);

    /**
     * Get first available entry.
     * @param key
     * @return first available entry.
     */
    public <T> T get(final String key, ObjectParser<T> objectParser) throws HedisException;

    /**
     * Replace new content to old content, return the old content if exits. It's suit to hash map
     * structure.
     * notice that, it will remove the all the entries with relate to key.
     * @param key
     * @param content The body need to put into Hedis.
     * @param objectParser The object transfer content body to byte array.
     * @param expireTimeDelta The time delta that the time point which expired.
     * @return old content, if not exits then return null
     */
    public <T> T put(final String key, T content, ObjectParser<T> objectParser, long expireTimeDelta) throws HedisException;

    /**
     * Delete all entries.
     * @param key
     */
    public void remove(final String key);

    /**
     * Get the tag of current hedis.
     */
    public String getTag();
}
