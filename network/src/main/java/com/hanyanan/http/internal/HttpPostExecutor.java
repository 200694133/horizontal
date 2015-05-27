package com.hanyanan.http.internal;

import com.hanyanan.http.CallBack;
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

import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/5/27.
 */
public class HttpPostExecutor extends HttpUrlExecutor {
    private static final String COLONSPACE = ": ";
    private static final String DASHDASH = "--";
    private static final String CRLF = "\r\n";
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

    private void writeRequestBodyMultipart(HttpRequest request, Map<String, Object> params, List<EntityHolder> entityHolders,
                                           URLConnection connection) throws IOException {
        String boundary = UUID.randomUUID().toString();
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        Map<String, String> encodedParam = encodeParams(params);
        ByteArrayBuffer buff = new ByteArrayBuffer();
        String boundaryLine =DASHDASH +  boundary;
        String boundaryEndLine =DASHDASH +  boundary + DASHDASH;
        //            --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC
//            Content-Disposition: form-data;name="desc"
//            Content-Type: text/plain; charset=UTF-8
//            Content-Transfer-Encoding: 8bit
//
//                    [......][......][......][......]...........................


        if(null != encodedParam && encodedParam.size() > 0){ //write request param
            Set<Map.Entry<String, String>> entries = encodedParam.entrySet();
            for(Map.Entry<String, String> entry : entries){
                StringBuilder data = new StringBuilder();
                data.append(boundaryLine)
                        .append(CRLF)
                        .append("Content-Disposition: form-data;name=\"").append(entry.getKey()).append("\"")
                        .append(CRLF)
                        .append(CRLF)
                        .append(entry.getValue())
                        .append(CRLF);
                outputStream.write(data.toString().getBytes());
            }
        }


        if(null != entityHolders && entityHolders.size() > 0) {
            CallBack callBack = request.getCallBack();
//            --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC
//            Content-Disposition: form-data;name="pic"; filename="photo.jpg"
//            Content-Type: application/octet-stream
//            Content-Transfer-Encoding: binary
//
//                    [图片二进制数据]
            for(EntityHolder entityHolder : entityHolders){
                long size = entityHolder.resource.size();
                StringBuilder data = new StringBuilder();
                data.append(boundaryLine)
                        .append(CRLF)
                        .append("Content-Disposition: form-data;name=\"").append(entityHolder.param).append("\"");
                if(!ValueUtil.isEmpty(entityHolder.fileName)){
                    data.append(COLONSPACE).append("filename=\"").append(entityHolder.fileName).append("\"");
                }
                data.append(CRLF)
                        .append("Content-Type: application/octet-stream")
                        .append(CRLF)
                        .append(CRLF);

            }
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
