package com.hyn.hedis;

import com.hyn.hedis.exception.HedisException;

import java.util.Collection;
import java.util.Map;

/**
 * Created by hanyanan on 2015/2/27.
 *  the list structure as follow:
 * |------------------------------------------------------------------------  linkedHashMap  ------------------------------------------------------------------------------------|
 * |---- primaryKey ----|--- tagkey ---|---- hashCode ----|---- priority ----|---- size ----|---- accessTime ----|---- modifyTime ----|---- createTime ----|---- expireTime ----|---- content ----|
 * |---- 0000000001 ----|---- map1 ----|---- 11111111 ----|----1232131321----|---- 1231 ----|---- 1231231232 ----|---- 7484873243 ----|---- 7484873243 ----|---- 1231231232 ----|----"12345678" --|
 * |---- 0000000002 ----|---- map1 ----|---- 22222222 ----|----1232122222----|---- 3434 ----|---- 3545454545 ----|---- 7484873243 ----|---- 7484873243 ----|---- 5465465546 ----|----"12332543" --|
 * |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * Column key1 and key2 as te primary key.
 * Store the
 */
public interface LinkedHashMapHedis extends BaseHedis {

    public static interface SetHedis<T> extends BaseHedis{
        public ObjectParser<T> getParser();

        /**
         * Returns the eldest entry in the map, or {@code null} if the map is empty.
         * @hide
         */
        public T eldest(OrderPolicy orderPolicy);


        /**
         * Replace new content to old content, return the old content if exits. It's suit to hash map
         * structure.
         * notice that, it will remove the all the entries with relate to key.
         * @param key
         * @param content The body need to put into Hedis.
         * @param expireTimeDelta The time delta that the time point which expired.
         * @return old content, if not exits then return null
         */
        public T put(final String key, T content,OrderPolicy orderPolicy,
                     long expireTimeDelta) throws HedisException;


        /**
         * Return  all elements.
         */
        public Collection<T> getAll(OrderPolicy orderPolicy);


        /**
         *
         * @param maxSize
         * @param orderPolicy
         * @param <T>
         * @return
         */
        public <T> Collection<T> trimSize(int maxSize, OrderPolicy orderPolicy);

        /**
         * Limit the max element count, remove illegal element. Delete the lowest order element
         * @param key the identify key.
         * @param maxRowCount max element count.
         * @param orderPolicy The policy that identify the out time.
         * @param <T>
         * @return All need removed elements.
         */
        public <T> Collection<T> trimCount(final String key, int maxRowCount, OrderPolicy orderPolicy);
    }

    public <T> SetHedis<T> getSetHedis(final String key);
    /**
     * Returns the eldest entry in the map, or {@code null} if the map is empty.
     * @hide
     */
    public <T> T eldest(final String key, ObjectParser<T> parser, OrderPolicy orderPolicy);

    /**
     * Limit the max element count, remove illegal element. Delete the lowest order element
     * @param key the identify key.
     * @param maxRowCount max element count.
     * @param parser The parse transfer byte array to content
     * @param orderPolicy The policy that identify the out time.
     * @param <T>
     * @return All need removed elements.
     */
    public <T> Collection<T> trimCount(final String key, int maxRowCount, ObjectParser<T> parser,
                                       OrderPolicy orderPolicy);

    /**
     *
     * @param key
     * @param maxSize
     * @param parser
     * @param orderPolicy
     * @param <T>
     * @return
     */
    public <T> Collection<T> trimSize(final String key, int maxSize,ObjectParser<T> parser,
                                      OrderPolicy orderPolicy);

}
