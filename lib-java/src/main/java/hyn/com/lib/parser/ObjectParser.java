package hyn.com.lib.parser;


import java.io.InputStream;

/**
 * Created by hanyanan on 2015/2/27.
 * Transfer object to byte array and transfer byte array to object.
 */
public interface ObjectParser<T> {
    /**
     * Return the byte array which come from input object.
     * @see #transferToObject(byte[])
     * @param object The data need to transfer to byte array
     */
    public byte[] transferToBlob(T object);

    /**
     * Return the input stream which come from object.
     * @param object the object need to transfer
     * @return the result
     */
    public InputStream transferToStream(T object);

    /**
     * Return the raw object which come from byte array.
     * @see #transferToBlob(Object)
     * @param blob The binary byte array.
     */
    public T transferToObject(byte[] blob) throws ParseFailedException;

    /**
     *
     * @param inputStream
     * @param autoClose
     * @return
     * @throws ParseFailedException
     */
    public T transferToObject(InputStream inputStream, boolean autoClose) throws ParseFailedException;
}
