package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpLoaderFactory;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;

import java.io.InputStream;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/6/14.
 */
public class HttpStringRequestJob extends AsyncJob<HttpRequest, TransportProgress, String> {
    public HttpStringRequestJob(HttpRequest request, JobCallback<TransportProgress, String> callback) {
        super(request, callback);
    }

    public HttpStringRequestJob(HttpRequest request, JobCallback<TransportProgress, String> callback,
                          CallbackDelivery callbackDelivery) {
        super(request, callback, callbackDelivery);
    }

    @Override
    public String performRequest() throws Throwable {
        HttpLoader loader = new HttpJobLoaderProxy(this, HttpLoaderFactory.createHttpExecutor(getParam()));
        HttpResponse response = null;
        InputStream inputStream = null;
        try{
            response = loader.performRequest(getParam());
            inputStream = response.body().getResource().openStream();
            byte[] data = IOUtil.inputStreamToBytes(inputStream);
            if(null == data || data.length <= 0) {
                return null;
            }
            return new String(data);
        }catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }finally {
            IOUtil.closeQuietly(inputStream);
            if(null != response) {
                response.dispose();
            }
        }
    }

}
