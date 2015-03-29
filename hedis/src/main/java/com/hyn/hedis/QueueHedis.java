package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import java.util.Collection;

/**
 * Created by hanyanan on 2015/2/15.
 * the list structure as follow:
 * |------------------------------------------------------------------------  list  ------------------------------------------------------------------------------------|
 * |---- key1 ----|---- key ----|---- priority ----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- 0001 ----|---- list1 --|----1232131321----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- 0002 ----|---- list1 --|----1232122222----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * Column key1 and key2 as te primary key.
 */
public interface QueueHedis extends BaseHedis{

    /**
     * Push content to the first position of list.
     * @param key the identify key.
     * @param content the content to push
     * @param parser The parse transfer content to byte array.
     * @param deltaExpireTime The delta expire mill times
     */
    public <T> void pushHead(final String key, T content, ObjectParser<T> parser, long deltaExpireTime);

    /**
     * Add content to tail of the list.
     * @param key the identify key.
     * @param content the content to push
     * @param parser The parse transfer content to byte array.
     * @param deltaExpireTime The delta expire mill times
     */
    public <T> void pushTail(final String key, T content, ObjectParser<T> parser,long deltaExpireTime);

    /**
     * Return  last element without remove it.
     * @param key the identify key.
     * @param parser The parse transfer byte array to content
     */
    public <T> T tail(final String key, ObjectParser<T> parser)throws HedisException;

    /**
     * Return first element without remove it.
     * @param key the identify key.
     * @param parser The parse transfer byte array to content
     */
    public <T> T head(final String key, ObjectParser<T> parser)throws HedisException;

    /**
     * Return the last element with remove it.
     * @param key the identify key.
     * @param parser The parse transfer byte array to content
     */
    public <T> T takeTail(final String key, ObjectParser<T> parser)throws HedisException;

    /**
     * Return the first element with remove it.
     * @param key the identify key.
     * @param parser The parse transfer byte array to content
     */
    public <T> T takeHead(final String key, ObjectParser<T> parser)throws HedisException;

    /**
     * Return  all elements.
     * @param key the identify key.
     * @param parser The parse transfer byte array to content
     */
    public <T> Collection<T> getAll(final String key, ObjectParser<T> parser) throws HedisException;

    /**
     * Delete the tail element.
     * @param key the identify key.
     */
    public void deleteTail(final String key);

    /**
     * Delete the first element.
     * @param key the identify key.
     */
    public void deleteFirst(final String key);


    /**
     * Limit the max element count, remove illegal element. Delete the lowest order element
     * @param key the identify key.
     * @param maxRowCount max element count.
     * @param parser The parse transfer byte array to content
     * @param <T>
     * @return All need removed elements.
     */
    public <T> Collection<T> trimCount(final String key, int maxRowCount, ObjectParser<T> parser) throws HedisException;

    /**
     *
     * @param key
     * @param maxSize
     * @param parser
     * @param <T>
     * @return
     */
    public <T> Collection<T> trimSize(final String key, int maxSize,ObjectParser<T> parser) throws HedisException;


    /**
     * Limit the max element count, remove illegal element. Delete the lowest order element
     * @param key the identify key.
     * @param maxRowCount max element count.
     * @return All need removed elements.
     */
    public void trimCountSilence(final String key, int maxRowCount);

    /**
     *
     * @param key
     * @param maxSize
     * @return
     */
    public void trimSizeSilence(final String key, int maxSize);

    //dispose all resource
    public void dispose();
}
