package com.hanyanan.http.job.download;


import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpResponseHeader;
import com.hanyanan.http.job.HttpJobFunction;
import com.hanyanan.http.job.HttpRequestJob;
import com.hyn.job.UnRetryException;
import java.io.InputStream;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/6/18.
 * 分段下载具体的某一部分的区域。
 */
public class DownloadBlockExecutor  {
    public static final int MAX_NOT_SAVED_SIZE = 1024 * 1024; // 2M
    private final VirtualFileDescriptor descriptor;

    DownloadBlockExecutor(VirtualFileDescriptor descriptor) {
        this.descriptor = descriptor;
    }


    public Void performRequest(HttpRequestJob asyncJob) throws Throwable{
        if (asyncJob.isCanceled()) {
            descriptor.abort();
            return null;
        }
        if (descriptor.isClosed()) {
            asyncJob.cancel();
            return null;
        }
        HttpJobFunction executor = HttpJobFunction.DEFAULT_EXECUTOR;
        HttpResponse response = null;
        InputStream inputStream = null;
        try {
//            response = executor.performRequest(asyncJob);
            HttpResponseHeader responseHeader = response.getResponseHeader();
            com.hanyanan.http.internal.Range range = responseHeader.getRange();
            /**
             * 先决判断,如果失败，会抛出不可重试的异常。
             */
            checkPrecondition(response, range);

            /*
            * 数据拷贝
            * */
            byte[] buff = new byte[64 * 1024]; // 64K
            long left = descriptor.length();
            int read = 0;
            long countOfNotSaved = 0;
            inputStream = response.body().getResource().openStream();
            while (left > 0 && (read = inputStream.read(buff)) > 0) {
                // 数据操作
                descriptor.write(buff, 0, read);
                left -= read;
                countOfNotSaved += read;
                if (countOfNotSaved >= MAX_NOT_SAVED_SIZE) {
                    descriptor.saveCurrentState();
                    countOfNotSaved = 0;
                }
            }
            descriptor.finish();
            return null;
        }catch (Throwable throwable) {
            // 不可重试
            if(!descriptor.isClosed()) {
                descriptor.saveCurrentState();
            }
            descriptor.abort();
            asyncJob.cancel();
            throw new UnRetryException("");
        }finally {
            IOUtil.closeQuietly(inputStream);
            if(null != response) {
                response.dispose();
            }
        }
    }


    /**
     * 进行先决条件的检测, 当与期望的大小不同时，则会尝试重新设置大小。
     *
     * @param response
     * @param range
     * @throws Throwable 当无法调整大小或起始位置不匹配时，会抛出无法重试的异常。
     */
    private void checkPrecondition(HttpResponse response, com.hanyanan.http.internal.Range range) throws Throwable {
        /*
         * 条件检查，是否能得到start和end属性, 如果有与期望的不同时，则尝试进行调整
         */
        long start = range.getStart();
        if (start != descriptor.offset()) {
            /*
             * 起始位置无法对应上，出现了异常，终止传输, 也不会在重试
             */
            response.dispose();
            descriptor.abort();
            throw new UnRetryException("Conflict start position!");
        }


        long end = range.getEnd();
        if (end < 0) {
            /*
             * 服务器没有回传长度和结束的位置，则默认下载到期望的为止。
             */
            end = descriptor.offset() + descriptor.length() - 1;
        }

        long length = end - start + 1; // real length could be transported
        if (length > descriptor.length()) {
            /*
             * 比期望的多，则只传递期望的数据后就会关闭连接。
             * */
        } else if (length == descriptor.length()) {
            /*
             * 严格的按照期望的进行
             * */
        } else {
            /*
             * 比期望的小， 则调整期望值
             * */
            descriptor.adjustNewLength(length);
        }
        // 调整成功
    }
}
