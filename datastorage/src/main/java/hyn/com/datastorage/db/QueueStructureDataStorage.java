package hyn.com.datastorage.db;

import java.io.InputStream;
import java.util.List;
import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.parser.ObjectParser;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/3/30.
 */
public interface QueueStructureDataStorage<T> extends BasicQueueStructureDataStorage {
    /**
     * The parse transfer content to byte array.
     * @return
     */
    public ObjectParser<T> getParser();
    /**
     * Push content to the first position of list.
     * @param content the content to push
     */
    public void pushHead(T content, Long expireTime);

    /**
     * Push content to the first position of list
     * @param inputStream the input stream
     */
    public void pushHead(InputStream inputStream, Long expireTime);

    /**
     * Add content to tail of the list.
     * @param content the content to push
     */
    public void pushTail(T content, Long expireTime);

    /**
     * Add content to tail of the list.
     * @param inputStream the input stream
     */
    public void pushTail(InputStream inputStream, Long expireTime);

    /**
     * Push content to the first position of list.
     * @param content the content to push
     */
    public void pushHead(T content);

    /**
     * Push content to the first position of list
     * @param inputStream the input stream
     */
    public void pushHead(InputStream inputStream);

    /**
     * Add content to tail of the list.
     * @param content the content to push
     */
    public void pushTail(T content);

    /**
     * Add content to tail of the list.
     * @param inputStream the input stream
     */
    public void pushTail(InputStream inputStream);

    /**
     * Return  last element without remove it.
     */
    public T tailObject() throws ParseFailedException;

    /**
     * Return  last element stream without remove it.
     */
    public BinaryResource tailStream();


    /**
     * Return first element without remove it.
     */
    public T headObject() throws ParseFailedException;

    /**
     * Return first element stream without remove it.
     */
    public BinaryResource headStream();

    /**
     * Return the last element with remove it.
     */
    public T takeTailObject() throws ParseFailedException;

    /**
     * Return the last element with remove it.
     */
    public BinaryResource takeTailStream();

    /**
     * Return the first element with remove it.
     */
    public T takeHeadObject() throws ParseFailedException;

    /**
     * Return the first element with remove it.
     */
    public BinaryResource takeHeadStream();

    /**
     * Return  all elements.
     */
    public List<T> getAllObject() throws ParseFailedException;
    /**
     * Return  current page elements.
     */
    public List<T> getPageObject(int pageIndex, int count) throws ParseFailedException;
}
