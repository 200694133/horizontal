package com.hanyanan.tiny.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Created by hanyanan on 2014/12/31.
 * 客户端请求如下：Range: bytes=5275648-
 * 服务器返回：Content-Length=106786028
 * 		   Accept-Ranges=bytes
 * 		   Content-Range=bytes 2000070-106786027/106786028
 *         如果使用Request带有Range,则返回206,
 *         否则返回200
 */
public class SimpleFileHttpRequestHandler implements HttpRequestHandler {
    public static class SimpleFileSet{
        public InputStream inputStream;
        public long size;
    }

    @Override
    public Copier getCopier() {
        return new DefaultCopier();
    }

    public SimpleFileSet getFile(String path, Map<String, String> requestParam,
                                 Map<String, String> requestHeaders, Map<String, String> responseHeaders) throws IOException{
//        FileInputStream inFileInputStream = new FileInputStream("/storage/extSdCard/DCIM/Camera/20141220_212114.mp4");
        File file = new File(path);
        SimpleFileSet res = new SimpleFileSet();
        res.inputStream = new FileInputStream(file);
        res.size = file.length();
        return res;
    }
    @Override
    public HandlerResult handle(String path, Map<String, String> requestParam, Map<String, String> requestHeaders) throws IOException{
        String rangeString = requestHeaders.get("Range");
        if(null == rangeString) rangeString = requestHeaders.get("RANGE");
        long[] range = new long[]{-1,-1};
        if(rangeString != null){
            range = TinyHttpHelper.getRange(rangeString);
            if(range == null || range.length != 2){
                range = new long[]{-1,-1};
            }
            if (range[1] == -1) {
                range[1] = Long.MAX_VALUE;
            }
        }
        if(range[0] == -1){
            range[0] = 0;
            range[1] = Long.MAX_VALUE;
        }
//
//		if(range[1] - range[0]+1>DEFAULT_POOL_SIZE){
//			range[1] = range[0] + DEFAULT_POOL_SIZE - 1;
//		}

        Log.d("get request range from " + range[0] + " to " + range[1]);
        HandlerResult response = new HandlerResult();
        String absPath = requestParam.get("path");
        absPath = absPath==null?"C:\\":absPath;
        absPath = URLDecoder.decode(absPath);
        SimpleFileSet file = getFile(absPath, requestParam, requestHeaders, response.mResponseHeaders);
        //test code
        final long fileLength = file.size;
        response.inputStream = null;
        if(null != rangeString){
            response.responseCode = 206;
        }else{
            response.responseCode = 200;
        }
        response.mResponseHeaders.put("Accept-Ranges", "bytes");
        response.mResponseHeaders.put("Content-Type", "application/octet-stream");
        response.mResponseHeaders.put("Server", "Tiny Http Server/0.1");
//		response.mResponseHead.put("Transfer-Encoding", "chunked");
        
        if(range[0]<fileLength && range[0] >= 0){//需要传递具体的内容
            long start = range[0];
            long end = range[1]>start?range[1]:Long.MAX_VALUE;
            end = end >fileLength-1?fileLength-1:end;
            response.size = end - start + 1;
            if(start > 0){
                file.inputStream.skip(start);
            }
            //Content-Range=bytes 2000070-106786027/106786028
            String contentRange = "bytes "+start+"-"+end+"/"+fileLength;
            response.mResponseHeaders.put("Content-Range", contentRange);
            response.mResponseHeaders.put("Content-Length", String.valueOf(response.size));
            response.inputStream = file.inputStream;
            Log.d("Real start "+start+" , real end "+end);
        }else{
            range[0] = -1;
            file.inputStream.close();
        }

        return response;
    }
}
