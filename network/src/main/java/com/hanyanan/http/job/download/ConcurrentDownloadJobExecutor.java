package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.Protocol;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.*;
import com.hanyanan.http.internal.Range;
import com.hanyanan.http.job.HttpJobExecutor;
import com.hanyanan.http.job.HttpRequestJob;
import com.hyn.job.AsyncJob;
import com.hyn.job.JobCallback;
import com.hyn.job.JobExecutor;
import com.hyn.job.UnexpectedResponseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/6/15.
 */
public class ConcurrentDownloadJobExecutor implements JobExecutor<HttpRequestJob, Float>,
        JobCallback<TransportProgress, HttpResponse> {
    private static final int DEFAULT_FIRST_FRAGMENT_SIZE = 20 * 1024; // 20K
    private static final int DEFAULT_FRAGMENT_COUNT = 2;
    private final File destFile;
    private final int concurrentCount = 3;
    private final long blockSize = 2 * 1024 * 1024; // 默认块大小
    private long position = 0;
    private long length = 0;
    private VirtualFileDescriptorProvider descriptorProvider;

    public ConcurrentDownloadJobExecutor(File dest) {
        this.destFile = dest;
    }

    @Override
    public Float performRequest(HttpRequestJob asyncJob) throws Throwable {
        HttpJobExecutor httpJobExecutor = HttpJobExecutor.DEFAULT_EXECUTOR;
        HttpRequest request = asyncJob.getParam();
        request.range(0, DEFAULT_FIRST_FRAGMENT_SIZE);
        HttpResponse response = httpJobExecutor.performRequest(asyncJob);
        try {
            if (!response.isSuccessful()) {
                UnexpectedResponseException exception = new UnexpectedResponseException();
                throw exception.unexpectedResponse(new UnexpectedResponseException.Result<HttpResponse>(response));
            }
            if (response.getProtocol() != Protocol.HTTP_1_1 || !supportRange(response.getRange())) {
                /*
                * Not support http 1.1 protocol.
                * */
                downloadDirect(response);
                return new Float(1);
            }

            Range range = response.getRange();
//            descriptorProvider = new DiscreteVirtualFileDescriptorProvider(destFile, range.getFullLength(), blockSize);
            for(int i = 0; i<DEFAULT_FRAGMENT_COUNT; ++i) {
//                VirtualFileDescriptor descriptor = descriptorProvider.deliveryAndLock();
//                HttpRequestJob job = nextFragment(request, descriptor);
//                JobLoader loader = JobLoader.getInstance();
//                loader.load(job);
            }
        } finally {
            response.dispose();
        }


        return null;
    }

//    private HttpRequestJob nextFragment(HttpRequestJob parentJob){
//        if(descriptorProvider.isClosed()) {
//            return null;
//        }
//        HttpRequest nextRequest = parentJob.getParam().clone();
//        nextRequest.range(parentJob.offset(), descriptor.length());
//        DownloadBlockExecutor executor = new DownloadBlockExecutor(descriptor); // TODO
//        HttpRequestJob job = new HttpRequestJob(nextRequest, executor, null);
//        return job;
//    }


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

    }

    @Override
    public void onSuccess(AsyncJob asyncJob, HttpResponse response) {

    }

    @Override
    public void onFailed(AsyncJob asyncJob, HttpResponse response, String msg, Throwable throwable) {

    }

    @Override
    public void onIntermediate(TransportProgress intermediate) {

    }
}
