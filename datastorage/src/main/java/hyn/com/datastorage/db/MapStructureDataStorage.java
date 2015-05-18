package hyn.com.datastorage.db;

import java.io.InputStream;
import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.android.parser.ObjectParser;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/3/30.
 */
public interface MapStructureDataStorage extends DeadlineStorage {
    /**
     * Put a new content to data storage.
     * @param key specify key
     * @param content new content
     * @param parser object parser
     */
    public <T> void put(final String key, T content, ObjectParser<T> parser);

    /**
     * Put a new content to data storage
     * @param key specify key
     * @param inputStream input stream
     */
    public <T> void put(final String key, final InputStream inputStream);

    /**
     * Put a new content to data storage.
     * @param key specify key
     * @param content new content
     * @param parser object parser
     */
    public <T> void put(final String key, T content, ObjectParser<T> parser, Long expireTime);

    /**
     * Put a new content to data storage
     * @param key specify key
     * @param inputStream input stream
     */
    public <T> void put(final String key, final InputStream inputStream, Long expireTime);

    /**
     * Return the specify entry,
     * @param key specify key
     * @param parser object parser
     * @return current content.
     */
    public <T> T get(final String key, ObjectParser<T> parser) throws ParseFailedException;

    /**
     * Get Measurable input stream for current key.
     * @param key specify key
     */
    public BinaryResource get(final String key);

    /**
     * Remove the specify entry.
     * @param key
     */
    public void remove(final String key);

    /** Remove all items. */
    public void clear();

    //
    public boolean isExists(String key);

    /** dispose current data storage! */
    public void dispose();
}
