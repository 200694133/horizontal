package hyn.com.datastorage.db;

import java.util.List;

/**
 * Created by hanyanan on 2015/4/20.
 */
public interface BasicQueueStructureDataStorage extends DeadlineStorage {
    //Get the tag if current queue.
    public String getTag();

    //Return the current size of this queue.
    public int size();
    //Return the count of current queue.
    public int count();

    /**
     * Push content to the first position of list.
     * @param content the content to push
     */
    public void pushHead(byte[] content, Long expireTime);

    /**
     * Add content to tail of the list.
     * @param content the content to push
     */
    public void pushTail(byte[] content, Long expireTime);

    /**
     * Push content to the first position of list.
     * @param content the content to push
     */
    public void pushHead(byte[] content);

    /**
     * Add content to tail of the list.
     * @param content the content to push
     */
    public void pushTail(byte[] content);

    /**
     * Return  last element without remove it.
     */
    public byte[] tail();

    /**
     * Return the last element with remove it.
     */
    public byte[] takeTail();
    /**
     * Return the first element without remove it.
     */
    public byte[] head();
    /**
     * Return the first element with remove it.
     */
    public byte[] takeHead();

    /**
     * Return  all elements.
     */
    public List<byte[]> getAll() ;
    /**
     * Return  current page elements.
     */
    public List<byte[]> getPage(int pageIndex, int count) ;

    /**
     * Delete the tail element.
     */
    public void deleteTail();

    /**
     * Delete the first element.
     */
    public void deleteHead();

    /**
     * Limit the max element count, remove illegal element. Delete the lowest order element
     * @param maxRowCount max element count.
     * @return All need removed elements.
     */
    public void trimCount(final int maxRowCount);

    /**
     *
     * @param maxSize
     * @return
     */
    public void trimSize(final int maxSize);


    /** dispose current data storage! */
    public void dispose();
}
