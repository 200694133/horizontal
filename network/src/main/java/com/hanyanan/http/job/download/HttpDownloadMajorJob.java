package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpLog;
import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.Protocol;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.*;
import com.hanyanan.http.internal.Range;
import com.hanyanan.http.job.HttpRequestJob;
import com.hanyanan.http.job.HttpRequestJobFunction;
import com.hyn.job.AsyncJob;
import com.hyn.job.JobCallback;
import com.hyn.job.JobLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/6/15.
 */
public class HttpDownloadMajorJob extends AsyncJob<HttpRequest,TransportProgress, Float>
        implements JobCallback<TransportProgress, Void> {
    private static final int DEFAULT_FIRST_FRAGMENT_SIZE = 20 * 1024; // 20K
    private static final int DEFAULT_FRAGMENT_COUNT = 2;
    private final File destFile;
    private final int concurrentCount = 3;
    private final long blockSize = 2 * 1024 * 1024; // 默认块大小
    private long finished = 0;
    private long length = 0;
    private VirtualFileDescriptorProvider descriptorProvider;

    public HttpDownloadMajorJob(HttpRequest param, JobCallback<TransportProgress, Float> callback, File destFile) {
        super(param, callback);
        this.destFile = destFile;
    }

    @Override
    public Float performRequest() throws Throwable {
        HttpRequestJobFunction httpRequestJobFunction = HttpRequestJobFunction.DEFAULT_EXECUTOR;
        HttpRequest request = getParam();
        request.range(0, DEFAULT_FIRST_FRAGMENT_SIZE);
        HttpResponse response = null; // httpJobFunction.performRequest(asyncJob);
        try {
            if (!response.isSuccessful()) {
                HttpLog.e(LOG_TAG, request.toString() + " get response code " + response.getCode());
                return new Float(0);
            }
            Range range = response.getRange();
            if (response.getProtocol() != Protocol.HTTP_1_1 || !supportRange(range)) {
                /*
                * Not support http 1.1 protocol.
                * */
                downloadDirect(response);
                return new Float(1);
            }


            descriptorProvider = new RandomFileDescriptorProvider(destFile, range.getFullLength(), blockSize);
            for(int i = 0; i<DEFAULT_FRAGMENT_COUNT; ++i) {
                HttpBlockDownloadJob job = nextBlock();
                if(null != job) {
                    JobLoader loader = JobLoader.getInstance();
                    loader.load(job);
                }
            }
        } finally {
            response.dispose();
        }


        return null;
    }


    private synchronized HttpBlockDownloadJob nextBlock(){
        HttpRequest request = getParam();
        if(descriptorProvider.isClosed()) {
            return null;
        }
        VirtualFileDescriptor descriptor = descriptorProvider.deliveryAndLock();
        if(null == descriptor) {
            return null;
        }
        HttpRequest nextRequest = request.clone();
        nextRequest.range(descriptor.offset(), descriptor.length());
        HttpBlockDownloadJob job = new HttpBlockDownloadJob(nextRequest, this, descriptor);
        return job;
    }


    private void downloadDirect(HttpResponse response) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(destFile);
        InputStream inputStream = response.body().getResource().openStream();
        IOUtil.copy(inputStream, fileOutputStream);
        IOUtil.closeQuietly(fileOutputStream);
        IOUtil.closeQuietly(inputStream);
    }

    /**
     * 不支持断点续传
     * */
    private boolean supportRange(com.hanyanan.http.internal.Range range) {
        /*
         * 条件检查，是否能得到start和end属性
         */
        long start = range.getStart();
        long end = range.getEnd();
        long length = range.getFullLength();
        if (length < 0) {
            return false;
        }

        return true;
    }

    @Override
    public void onCanceled(AsyncJob asyncJob) {
        // TODO
    }

    @Override
    public void onSuccess(AsyncJob asyncJob, Void response) {
        HttpBlockDownloadJob job = nextBlock();
        if(null != job) {
            JobLoader loader = JobLoader.getInstance();
            loader.load(job);
        }
    }

    @Override
    public void onFailed(AsyncJob asyncJob, Void response, String msg, Throwable throwable) {
        VirtualFileDescriptor descriptor = ((HttpBlockDownloadJob)asyncJob).descriptor;
        descriptor.abort();

        HttpBlockDownloadJob job = nextBlock();
        if(null != job) {
            JobLoader loader = JobLoader.getInstance();
            loader.load(job);
        }
    }

    @Override
    public void onIntermediate(AsyncJob asyncJob, TransportProgress intermediate) {

    }
}
