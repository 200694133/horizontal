package com.hanyanan.http;

import com.hanyanan.http.internal.HttpResponse;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import hyn.com.lib.IOUtil;
import hyn.com.lib.binaryresource.BinaryResource;
import hyn.com.lib.binaryresource.ByteArrayBinaryResource;

/**
 * Created by hanyanan on 2015/5/29.
 */
public class MainTest {

    public static void main(String []argv) {
        HttpService service = HttpService.getInstance();
        String url = "http://httpbin.org/post";
        HttpRequest request = new HttpRequest(url, Method.POST);
        Map<String, String> params = new HashMap<String, String>();
        params.put("cityid", "100010000");
        params.put("url", "http://httpbin.org/redirect-to?url=http://httpbin.org/get");
        request.params(params);
        HttpRequestBody body = new HttpRequestBody();
        body.add("json",new ByteArrayBinaryResource("{data:dddddddddddddddddddddddddddddddddddddddddd}".getBytes()));
        request.setHttpRequestBody(body);
        try {
            HttpResponse response = service.loadHttpRequest(request);
            if(!response.isSuccessful()){
                System.out.println(response.toString());
                return ;
            }
            BinaryResource resource = response.body().getResource();
            InputStream stream = resource.openStream();
            byte[] data = IOUtil.getBytesFromStream(stream);
            System.out.println(new String(data));
            IOUtil.closeQuietly(stream);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
