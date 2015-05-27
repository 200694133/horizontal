package com.hanyanan.http.internal;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.HttpRequestBody.EntityHolder;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by hanyanan on 2015/5/27.
 */
public class HttpPostExecutor extends HttpUrlExecutor {
    private static final byte[] COLONSPACE = { ':', ' ' };
    private static final byte[] DASHDASH = { '-', '-' };
    @Override protected void writeRequestBody(HttpRequest request, URLConnection connection) {
        String url = request.getUrl();
        Map<String, Object> params = request.getParams();
        List<EntityHolder> entityHolders = request.getRequestBody().getResources();
        if(params.size() <= 0 && entityHolders.size() <= 0) {
            System.out.println("url post request not need upload anything.");
            return ;
        }
        if(entityHolders.size() > 0) {

        }
    }

    private void writeRequestBodyMultipart(Map<String, Object> params, List<EntityHolder> entityHolders,
                                           URLConnection connection) throws IOException {
        String boundary = UUID.randomUUID().toString();
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        Map<String, String> encodedParam = encodeParams(params);
        ByteArrayBuffer buff = new ByteArrayBuffer();
        byte[] boundaryEndLine =(new String(DASHDASH) +  boundary).getBytes();
        if(null != encodedParam && encodedParam.size() > 0){ //write request param

        }
    }

    private Map<String, String> encodeParams(Map<String, Object> params){
        if(null == params || params.size() <= 0) return null;
        Map<String, String> res = new HashMap<String, String>();
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        for(Map.Entry<String, Object> entry : entries){
            try {
                res.put(URLEncoder.encode(entry.getKey(), "UTF-8"),
                        URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
