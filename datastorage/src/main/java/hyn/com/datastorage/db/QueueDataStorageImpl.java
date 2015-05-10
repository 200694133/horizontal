package hyn.com.datastorage.db;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.lib.IOUtil;
import hyn.com.lib.android.logging.Log;
import hyn.com.lib.binaryresource.BinaryResource;
import hyn.com.lib.binaryresource.ByteArrayBinaryResource;

/**
 * Created by hanyanan on 2015/4/4.
 * 一个tag只能对应一种实例，不能同时分别对应限时和非限时的情况！
 */
public class QueueDataStorageImpl<T> extends BasicQueueDataStorageImpl implements QueueStructureDataStorage<T> {
    public static final String TAG = "QueueDataStorageImpl";
    protected final ObjectParser<T> objectParser;

    public QueueDataStorageImpl(String tag, ObjectParser<T> parser, BasicDataBaseHelper basicDataBaseHelper) {
        super(tag, basicDataBaseHelper);
        objectParser = parser;
    }

    @Override
    public ObjectParser<T> getParser() {
        return objectParser;
    }


    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间的负值作为优先级。确保
     * 优先级数值为最小，从而排在前面
     *
     * @see #pushHead(T)
     * @see #pushHead(java.io.InputStream)
     * @see #pushHead(java.io.InputStream, Long)
     * @see #pushTail(java.io.InputStream)
     * @see #pushTail(java.io.InputStream, Long)
     * @see #pushTail(T)
     * @see #pushTail(T, Long)
     * @param content the content to push
     * @param expireTime
     */
    public void pushHead(T content, Long expireTime) {
        byte[] body = getParser().transferToBlob(content);
        pushHead(body, expireTime);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间的负值作为优先级。确保
     * 优先级数值为最小，从而排在前面
     *
     * @see #pushHead(T)
     * @see #pushHead(java.io.InputStream)
     * @see #pushHead(T, Long)
     * @see #pushTail(java.io.InputStream)
     * @see #pushTail(java.io.InputStream, Long)
     * @see #pushTail(T)
     * @see #pushTail(T, Long)
     * @param inputStream the input stream to push
     * @param expireTime
     */
    public void pushHead(InputStream inputStream, Long expireTime) {
        byte[] body = IOUtil.inputStreamToBytes(inputStream);
        if(null == body || body.length == 0) {
            Log.e(TAG, "pushHead cannot read anything from input stream!");
            return ;
        }
        pushHead(body, expireTime);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间的负值作为优先级。确保
     * 优先级数值为最小，从而排在前面
     *
     * @see #pushHead(java.io.InputStream)
     * @see #pushHead(T, Long)
     * @see #pushHead(java.io.InputStream, Long)
     * @see #pushTail(java.io.InputStream)
     * @see #pushTail(T, Long)
     * @see #pushTail(T)
     * @see #pushTail(InputStream, Long)
     *
     * @param content the content to push
     */
    public void pushHead(T content) {
        byte[] body = getParser().transferToBlob(content);
        if(null == body || body.length == 0) {
            Log.e(TAG, "pushHead cannot read anything from input stream!");
            return ;
        }
        pushHead(body);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间的负值作为优先级。确保
     * 优先级数值为最小，从而排在前面
     *
     * @see #pushHead(T)
     * @see #pushHead(T, Long)
     * @see #pushHead(java.io.InputStream, Long)
     * @see #pushTail(java.io.InputStream)
     * @see #pushTail(T, Long)
     * @see #pushTail(T)
     * @see #pushTail(InputStream, Long)
     *
     * @param inputStream inputStream the input stream to push
     */
    public void pushHead(InputStream inputStream) {
        byte[] body = IOUtil.inputStreamToBytes(inputStream);
        if(null == body || body.length == 0) {
            Log.e(TAG, "pushHead cannot read anything from input stream!");
            return ;
        }
        pushHead(body);
    }
    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间作为优先级。确保
     * 优先级数值为最大，从而排在后面
     *
     * @see #pushHead(T)
     * @see #pushHead(java.io.InputStream)
     * @see #pushHead(T, Long)
     * @see #pushHead(java.io.InputStream, Long)
     * @see #pushTail(java.io.InputStream)
     * @see #pushTail(java.io.InputStream, Long)
     * @see #pushTail(T)
     *
     * @param content the content to push
     * @param expireTime
     */
    public void pushTail(T content, Long expireTime) {
        byte[] body = getParser().transferToBlob(content);
        if(null == body || body.length == 0) {
            Log.e(TAG, "pushHead cannot read anything from input stream!");
            return ;
        }
        pushTail(body, expireTime);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间作为优先级。确保
     * 优先级数值为最大，从而排在后面
     *
     * @see #pushHead(T)
     * @see #pushHead(java.io.InputStream)
     * @see #pushHead(T, Long)
     * @see #pushHead(java.io.InputStream, Long)
     * @see #pushTail(java.io.InputStream)
     * @see #pushTail(T, Long)
     * @see #pushTail(T)
     *
     * @param inputStream the input stream to push
     * @param expireTime
     */
    public void pushTail(InputStream inputStream, Long expireTime) {
        byte[] body = IOUtil.inputStreamToBytes(inputStream);
        if(null == body || body.length == 0) {
            Log.e(TAG, "pushTail cannot read anything from input stream!");
            return ;
        }
        pushTail(body, expireTime);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间作为优先级。确保
     * 优先级数值为最大，从而排在后面
     *
     * @see #pushHead(T)
     * @see #pushHead(java.io.InputStream)
     * @see #pushHead(T, Long)
     * @see #pushHead(java.io.InputStream, Long)
     * @see #pushTail(java.io.InputStream)
     * @see #pushTail(T, Long)
     * @see #pushTail(java.io.InputStream, Long)
     *
     * @param content the content to push
     */
    public void pushTail(T content) {
        byte[] body = getParser().transferToBlob(content);
        if(null == body || body.length == 0) {
            Log.e(TAG, "pushHead cannot read anything from input stream!");
            return ;
        }
        pushTail(body);
    }

    /**
     * 添加的策略为优先级属性，优先级数值越小，优先级越高，该方法会将当前时间作为优先级。确保
     * 优先级数值为最大，从而排在后面
     *
     * @see #pushHead(T)
     * @see #pushHead(java.io.InputStream)
     * @see #pushHead(T, Long)
     * @see #pushHead(java.io.InputStream, Long)
     * @see #pushTail(T)
     * @see #pushTail(T, Long)
     * @see #pushTail(java.io.InputStream, Long)
     *
     * @param inputStream the input stream to push
     */
    public void pushTail(InputStream inputStream) {
        byte[] body = IOUtil.inputStreamToBytes(inputStream);
        if(null == body || body.length == 0) {
            Log.e(TAG, "pushHead cannot read anything from input stream!");
            return ;
        }
        pushTail(body);
    }

    @Override
    public T tailObject() throws ParseFailedException {
        byte[] content = tail();
        if(null == content || content.length == 0) return null;
        return getParser().transferToObject(content);
    }

    @Override
    public BinaryResource tailStream() {
        byte[] content = tail();
        if(null == content || content.length == 0) return null;
        return new ByteArrayBinaryResource(content);
    }

    @Override
    public T headObject() throws ParseFailedException {
        byte[] content = head();
        if(null == content || content.length == 0) return null;
        return getParser().transferToObject(content);
    }

    @Override
    public BinaryResource headStream() {
        byte[] content = head();
        if(null == content || content.length == 0) return null;
        return new ByteArrayBinaryResource(content);
    }

    @Override
    public T takeTailObject() throws ParseFailedException {
        T res = tailObject();
        deleteTail();
        return res;
    }

    @Override
    public BinaryResource takeTailStream() {
        BinaryResource res = tailStream();
        deleteTail();
        return res;
    }

    @Override
    public T takeHeadObject() throws ParseFailedException {
        T res = headObject();
        deleteHead();
        return res;
    }

    @Override
    public BinaryResource takeHeadStream() {
        BinaryResource res = headStream();
        deleteHead();
        return res;
    }

    @Override
    public List<T> getAllObject() throws ParseFailedException {
        List<byte[]> res = getAll();
        if(null == res || res.size() <= 0) return null;
        List<T> out = new ArrayList<>();
        for(byte[] body:res){
            if(null == body || body.length == 0) continue;
            out.add(getParser().transferToObject(body));
        }
        return out;
    }

    @Override
    public List<T> getPageObject(int pageIndex, int count) throws ParseFailedException {
        List<byte[]> res = getPage(pageIndex,count);
        if(null == res || res.size() <= 0) return null;
        List<T> out = new ArrayList<>();
        for(byte[] body:res){
            if(null == body || body.length == 0) continue;
            out.add(getParser().transferToObject(body));
        }
        return out;
    }
}
