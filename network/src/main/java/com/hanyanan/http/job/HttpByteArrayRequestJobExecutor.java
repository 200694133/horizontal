package com.hanyanan.http.job;

import com.hanyanan.http.internal.HttpResponse;
import com.hyn.job.JobExecutor;

import java.io.InputStream;

import hyn.com.lib.IOUtil;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by n550 on 2015/6/14.
 */
public class HttpByteArrayRequestJobExecutor implements JobExecutor<HttpRequestJob, byte[]> {
    public static HttpByteArrayRequestJobExecutor DEFAULT_BYTE_JOB_EXECUTOR = new HttpByteArrayRequestJobExecutor();
    @Override
    public byte[] performRequest(HttpRequestJob asyncJob) throws Throwable {
        HttpJobLoaderProxy executor = HttpJobLoaderProxy.DEFAULT_EXECUTOR;
        HttpResponse response = executor.performRequest(asyncJob);
        BinaryResource resource = response.body().getResource();
        InputStream stream = resource.openStream();
        byte[] data = new byte[0];
        data = IOUtil.getBytesFromStream(stream);
        IOUtil.closeQuietly(stream);
        return data;
    }
}
