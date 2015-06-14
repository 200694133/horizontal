package com.hanyanan.http.job;

import com.hanyanan.http.TransportProgress;
import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.JobExecutor;

/**
 * Created by hanyanan on 2015/6/14.
 */
public class HttpStringJobExecutor implements JobExecutor<HttpRequestJob, String> {

    @Override
    public String performRequest(HttpRequestJob asyncJob) throws Throwable {
        byte[] result = HttpByteArrayRequestJobExecutor.DEFAULT_BYTE_JOB_EXECUTOR.performRequest(asyncJob);
        return new String(result);
    }
}
