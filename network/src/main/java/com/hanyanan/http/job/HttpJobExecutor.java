package com.hanyanan.http.job;

import com.hanyanan.http.Method;
import com.hanyanan.http.internal.HttpGetExecutor;
import com.hanyanan.http.internal.HttpPostExecutor;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpUrlExecutor;
import com.hyn.job.JobExecutor;

/**
 * Created by hanyanan on 2015/6/12.
 */
public class HttpJobExecutor implements JobExecutor<HttpRequestJob, HttpResponse> {
    @Override
    public HttpResponse performRequest(HttpRequestJob asyncJob) throws Throwable {
        HttpUrlExecutor httpUrlExecutor;
        if(asyncJob.getParam().getMethod() == Method.GET) {
            httpUrlExecutor = new HttpGetExecutor();
        }else{
            httpUrlExecutor = new HttpPostExecutor();
        }
        HttpResponse response = httpUrlExecutor.run(asyncJob.getParam());
        return response;
    }
}