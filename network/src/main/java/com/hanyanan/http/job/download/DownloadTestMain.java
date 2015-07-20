package com.hanyanan.http.job.download;

import com.hanyanan.http.Headers;
import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.HttpRequestBody;
import com.hanyanan.http.Method;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.Range;
import com.hanyanan.http.job.HttpRequestJobFunction;
import com.hanyanan.http.job.HttpResponseFunction;
import com.hyn.job.AsyncJob;
import com.hyn.job.FunctionAsyncJob;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;
import com.hyn.job.log.Log;
import com.hyn.job.processor.BytesToStringFunction;
import com.hyn.job.processor.InputToBytesFunction;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hyn.com.lib.ValueUtil;
import hyn.com.lib.binaryresource.ByteArrayBinaryResource;

/**
 * Created by hanyanan on 2015/7/20.
 */
public class DownloadTestMain  {
    public static void main(String[] argv) {
        String url = "http://down22.huacolor.com:8080/down/feijia/201502/baidunuomi_V5.7.0.apk";
        HttpRequest request = new HttpRequest(url, Method.GET);
//        HttpRequestBody body = new HttpRequestBody();
//        body.add("json", new ByteArrayBinaryResource("{data:dddddddddddddddddddddddddddddddddddddddddd}".getBytes()));
//        request.setHttpRequestBody(body);


        JobCallback<TransportProgress, Float> downloadCallback = new JobCallback<TransportProgress, Float>(){

            @Override
            public void onCanceled(AsyncJob asyncJob) {

            }

            @Override
            public void onSuccess(AsyncJob asyncJob, Float response) {
                System.out.println(response);
            }

            @Override
            public void onFailed(AsyncJob asyncJob, Float response, String msg, Throwable throwable) {

            }

            @Override
            public void onIntermediate(AsyncJob asyncJob, TransportProgress intermediate) {
                Log.d("ddd", "" + intermediate.getCurrentPosition() + "\t" + intermediate.getCount());
            }
        };

        File file = new File("D://d//bainuo_5.7.apk");
        HttpDownloadMajorJob httpJob = new HttpDownloadMajorJob(request, downloadCallback, file, 512 * 1024);
//                new FunctionAsyncJob.Builder<HttpRequest, TransportProgress, HttpResponse>()
//                        .setCallback(stringCallback)
//                        .setParam(request)
//                        .addProcessor(new HttpRequestJobFunction())
//                        .addProcessor(new HttpResponseFunction())
//                        .addProcessor(new InputToBytesFunction(true))
//                        .addProcessor(new BytesToStringFunction())
//                        .build();
        JobLoader jobLoader = JobLoader.getInstance();
        jobLoader.load(httpJob);
    }
}
