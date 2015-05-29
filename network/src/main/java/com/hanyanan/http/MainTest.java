package com.hanyanan.http;

import com.hanyanan.http.internal.HttpGetExecutor;
import com.hanyanan.http.internal.HttpPostExecutor;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpUrlExecutor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import hyn.com.lib.IOUtil;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/29.
 */
public class MainTest {

    public static void main(String []argv) {
        HttpUrlExecutor httpUrlExecutor = new HttpPostExecutor();
        String url = "http://httpbin.org/user-agent";
        HttpRequest request = new HttpRequest(url, Method.POST);
        Map<String, String> params = new HashMap<String, String>();
        params.put("cityid", "100010000");
        request.params(params);
        try {
            HttpResponse response = httpUrlExecutor.run(request);
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
