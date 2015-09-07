package com.hanyanan.tiny.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/12/31.
 */
public interface HttpRequestHandler {
    /**
     * The result of the request.
     */
    public static class HandlerResult{
        /** The result stream to transfer to client. Cannot get raw file size from available method. */
        public InputStream inputStream;
        /** the length of result  */
        public long size = Long.MAX_VALUE;
        /** the header need to response to client. */
        public final HashMap<String,String> mResponseHeaders = new HashMap<String,String>();
        /** Status code */
        public int responseCode;

        public HandlerResult(){

        }

        public HandlerResult(int code){
            responseCode = code;
        }
    }

    /** Get the copier, user can custom it to make traffic control or encode data. */
    public Copier getCopier();

    /**
     * handle the request and response the result.
     * @param path request url path
     * @param requestParam request param
     * @param requestHeaders request header
     * @return
     */
    public HandlerResult handle(String path, Map<String, String> requestParam,
                                Map<String, String> requestHeaders) throws IOException;
}
