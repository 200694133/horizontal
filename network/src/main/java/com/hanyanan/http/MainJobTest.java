package com.hanyanan.http;

import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.job.HttpRequestJobFunction;
import com.hanyanan.http.job.HttpResponseFunction;
import com.hyn.job.AsyncJob;
import com.hyn.job.FunctionAsyncJob;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;
import com.hyn.job.log.Log;
import com.hyn.job.processor.BytesToStringFunction;
import com.hyn.job.processor.InputToBytesFunction;

import java.util.HashMap;
import java.util.Map;

import hyn.com.lib.binaryresource.ByteArrayBinaryResource;

/**
 * Created by hanyanan on 2015/6/13.
 */
public class MainJobTest {
    public static void main(String []argv) {
        String url = "http://httpbin.org/post";
        HttpRequest request = new HttpRequest(url, Method.POST);
        Map<String, String> params = new HashMap<String, String>();
        params.put("cityid", "100010000");
        params.put("url", "http://httpbin.org/redirect-to?url=http://httpbin.org/get");
        request.params(params);
        HttpRequestBody body = new HttpRequestBody();
        body.add("json", new ByteArrayBinaryResource("{data:dddddddddddddddddddddddddddddddddddddddddd}".getBytes()));
        request.setHttpRequestBody(body);

//        JobCallback<TransportProgress, HttpResponse> callback = new JobCallback<TransportProgress, HttpResponse>(){
//
//            @Override
//            public void onCanceled(AsyncJob asyncJob) {
//
//            }
//
//            @Override
//            public void onSuccess(AsyncJob asyncJob, HttpResponse response) {
//                if(!response.isSuccessful()){
//                    System.out.println(response.toString());
//                    return ;
//                }
//
//                try {
//                    BinaryResource resource = response.body().getResource();
//                    InputStream stream = resource.openStream();
//                    byte[] data = new byte[0];
//                    data = IOUtil.getBytesFromStream(stream);
//                    System.out.println(new String(data));
//                    IOUtil.closeQuietly(stream);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailed(AsyncJob asyncJob, HttpResponse response, String msg, Throwable throwable) {
//
//            }
//
//            @Override
//            public void onIntermediate(AsyncJob asyncJob, TransportProgress intermediate) {
//                Log.d("ddd",""+intermediate.getCurrentPosition()+"\t"+intermediate.getCount());
//            }
//        };


        JobCallback<TransportProgress, String> stringCallback = new JobCallback<TransportProgress, String>(){

            @Override
            public void onCanceled(AsyncJob asyncJob) {

            }

            @Override
            public void onSuccess(AsyncJob asyncJob, String response) {
                    System.out.println(response);
            }

            @Override
            public void onFailed(AsyncJob asyncJob, String response, String msg, Throwable throwable) {

            }

            @Override
            public void onIntermediate(AsyncJob asyncJob, TransportProgress intermediate) {
                Log.d("ddd",""+intermediate.getCurrentPosition()+"\t"+intermediate.getCount());
            }
        };

        AsyncJob<HttpRequest, TransportProgress, HttpResponse> httpJob =
                new FunctionAsyncJob.Builder<HttpRequest, TransportProgress, HttpResponse>()
                        .setCallback(stringCallback)
                        .setParam(request)
                        .addProcessor(new HttpRequestJobFunction())
                        .addProcessor(new HttpResponseFunction())
                        .addProcessor(new InputToBytesFunction(true))
                        .addProcessor(new BytesToStringFunction())
                        .build();
        JobLoader jobLoader = JobLoader.getInstance();
        jobLoader.load(httpJob);
    }
}
