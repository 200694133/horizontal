package com.hanyanan.http.job.download;

import com.hanyanan.http.DefaultHttpRetryPolicy;
import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.job.HttpFingerPrint;
import com.hanyanan.http.job.HttpJobExecutor;
import com.hanyanan.http.job.HttpRequestJob;
import com.hyn.job.AsyncJob;
import com.hyn.job.CallbackDelivery;
import com.hyn.job.JobCallback;
import com.hyn.job.JobExecutor;
import com.hyn.job.PriorityPolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2015/7/7.
 */
public class ConcurrentHttpRequestJob extends HttpRequestJob implements JobExecutor<ConcurrentHttpRequestJob, HttpResponse> {
    private static final int DEFAULT_FIRST_FRAGMENT_SIZE = 20 * 1024; // 20K
    private static final int DEFAULT_FRAGMENT_COUNT = 2;
    private File destFile;
    private final int concurrentCount = 3;
    private final long blockSize = 2 * 1024 * 1024; // 默认块大小
    private long position = 0;
    private long length = 0;
    private VirtualFileDescriptorProvider descriptorProvider;
    private final List<AsyncJob> childrenJobs = new ArrayList<AsyncJob>();
    public ConcurrentHttpRequestJob(HttpRequest request, JobCallback<TransportProgress, HttpResponse> callback) {

    }


    @Override
    public HttpResponse performRequest(ConcurrentHttpRequestJob asyncJob) throws Throwable {
        return null;
    }

    private static class JobExecutorProxy
}
