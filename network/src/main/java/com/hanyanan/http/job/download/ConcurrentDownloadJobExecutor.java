package com.hanyanan.http.job.download;

import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.job.HttpRequestJob;
import com.hyn.job.JobExecutor;

import java.io.File;

/**
 * Created by hanyanan on 2015/6/15.
 */
public class ConcurrentDownloadJobExecutor implements JobExecutor<HttpRequestJob, Integer> {
    private final File destFile;

    public ConcurrentDownloadJobExecutor(File dest){
        this.destFile = dest;
    }

    @Override
    public Integer performRequest(HttpRequestJob asyncJob) throws Throwable {
        return null;
    }
}