package hyn.com.storage;


import hyn.com.lib.LargeInputStream;
import hyn.com.lib.TwoTuple;

/**
 * Created by hanyanan on 2015/2/12.
 * Support tuple key. Support two tuple as the search key.
 */
public interface IDBStorage {
    /**
     * get all length of storage module used.
     * @return length of current storage used.
     */
    public long length();

    /**
     * get used length
     * @param key
     * @return length of current element used.
     */
    public long length(String key);

    /**
     * get used length
     * @param key
     * @return length of current element used.
     */
    public long length(TwoTuple<String,String> key);

    /**
     * Query
     * @param key
     * @return
     */
    public LargeInputStream query(TwoTuple<String,String> key);

    public LargeInputStream query(String key);


}
