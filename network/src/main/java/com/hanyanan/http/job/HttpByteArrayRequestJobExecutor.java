package com.hanyanan.http.job;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpLoader;
import com.hanyanan.http.internal.HttpLoaderFactory;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobExecutor;
import com.hyn.job.PriorityPolicy;
import com.hyn.job.RetryPolicy;

import java.io.InputStream;

import hyn.com.lib.Fingerprint;
import hyn.com.lib.IOUtil;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/6/14.
 */
public class HttpByteArrayRequestJobExecutor extends AsyncJob<HttpRequest, TransportProgress, byte[]> {

    public HttpByteArrayRequestJobExecutor(HttpRequest param, JobCallback<TransportProgress, byte[]> callback, CallbackDelivery callbackDelivery, RetryPolicy retryPolicy, PriorityPolicy priorityPolicy, Fingerprint fingerprint, JobExecutor<? extends AsyncJob, byte[]> jobExecutor) {
        super(param, callback, callbackDelivery, retryPolicy, priorityPolicy, fingerprint, jobExecutor);
    }

    public HttpByteArrayRequestJobExecutor(HttpRequest param, JobCallback<TransportProgress, byte[]> callback, CallbackDelivery callbackDelivery) {
        super(param, callback, callbackDelivery);
    }

    public HttpByteArrayRequestJobExecutor(HttpRequest param, JobCallback<TransportProgress, byte[]> callback) {
        super(param, callback);
    }

    public HttpByteArrayRequestJobExecutor(HttpRequest param, JobCallback<TransportProgress, byte[]> callback, CallbackDelivery callbackDelivery, RetryPolicy retryPolicy) {
        super(param, callback, callbackDelivery, retryPolicy);
    }

    public HttpByteArrayRequestJobExecutor(HttpRequest param, JobCallback callback, RetryPolicy retryPolicy) {
        super(param, callback, retryPolicy);
    }

    @Override
    public byte[] performRequest() throws Throwable {
        HttpLoader loader = new HttpJobLoaderProxy(this, HttpLoaderFactory.createHttpExecutor(getParam()));
        HttpResponse response = null;
        InputStream inputStream = null;
        try{
            response = loader.performRequest(getParam());
            inputStream = response.body().getResource().openStream();
            byte[] data = IOUtil.inputStreamToBytes(inputStream);
            return data;
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
