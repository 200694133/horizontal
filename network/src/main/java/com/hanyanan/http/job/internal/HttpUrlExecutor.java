package com.hanyanan.http.job.internal;

import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.job.HttpRequestJob;
import com.hyn.job.JobExecutor;

/**
 * Created by hanyanan on 2015/6/29.
 */
public class HttpUrlExecutor implements JobExecutor<HttpRequestJob, HttpResponse> {
    public static final int HTTP_TEMP_REDIRECT = 307;
    public static final int HTTP_PERM_REDIRECT = 308;
    public static final int MAX_REDIRECT_COUNT = 10;




    @Override
    public HttpResponse performRequest(HttpRequestJob asyncJob) throws Throwable {
        return null;
    }
}
