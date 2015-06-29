package com.hanyanan.http.job.download;

import com.hanyanan.http.HttpRequest;
import com.hanyanan.http.internal.HttpResponse;
import com.hanyanan.http.internal.HttpResponseHeader;
import com.hanyanan.http.job.HttpJobExecutor;
import com.hanyanan.http.job.HttpRequestJob;
import com.hyn.job.JobExecutor;
import com.hyn.job.UnRetryException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2015/6/18.
 * 分段下载具体的某一部分的区域。
 */
public class DownloadBlockExecutor implements JobExecutor<HttpRequestJob, Void> {
    private final VirtualFileDescriptor descriptor;

    DownloadBlockExecutor(VirtualFileDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    private boolean checkAbort(HttpRequestJob asyncJob, HttpResponse response) {
        return asyncJob.isCanceled() || descriptor.isClosed();
    }

    @Override
    public Void performRequest(HttpRequestJob asyncJob) {
        HttpRequest request = asyncJob.getParam();
        HttpJobExecutor baseJobExecutor = HttpJobExecutor.DEFAULT_EXECUTOR;
        HttpResponse response = baseJobExecutor.performRequest(asyncJob);
        HttpResponseHeader responseHeader = response.getResponseHeader();
        com.hanyanan.http.internal.Range range = responseHeader.getRange();

        /**
         * 先决判断,如果失败，会抛出不可重试的异常。
         */
        checkPrecondition(response, range);

        InputStream inputStream = null;
        try {
            inputStream = response.body().getResource().openStream();
        } catch (IOException e) {
            e.printStackTrace();
            response.dispose();
            throw e;
        }

        {
            /*
            * 检查当前状态
            * */
            if (checkAbort(asyncJob, response)) {
                response.dispose();
                descriptor.abort();
                if (!asyncJob.isCanceled()) {
                    asyncJob.cancel();
                }
                return null;
            }
        }

        {
            /*
            * 数据拷贝
            * */
            byte[] buff = new byte[64 * 1024]; // 64K
            long left = descriptor.length();
            int read = 0;
            long totalRead = 0;
            try {
                while (left > 0 && (read = inputStream.read(buff)) > 0) {
                    {
                        /*
                        * 检查当前状态
                        * */
                        if (checkAbort(asyncJob, response)) {
                            response.dispose();
                            descriptor.abort();
                            if (!asyncJob.isCanceled()) {
                                asyncJob.cancel();
                            }
                            return null;
                        }
                    }

                    // 数据操作
                    descriptor.write(buff, 0, read);
                    left -= read;
                    totalRead += read;
                }
            } catch (IOException e) {
                response.dispose();
                /*
                * 读取失败，可能是网络原因，可能是写操作被关闭了
                * */
                if (checkAbort(asyncJob, response)) {
                    asyncJob.cancel();
                    descriptor.abort();
                    response.dispose();
                    return null;
                }

                throw new UnRetryException(e);
            }
            if (checkAbort(asyncJob, response)) {
                asyncJob.cancel();
                descriptor.abort();
                response.dispose();
                return null;
            }
            if (left > 0) {
                /*
                * 没有读取期望的大小，读取失败
                * */
                descriptor.abort();
                response.dispose();
                throw new UnRetryException("Cannot read the expect length!Download failed!");
            }
        }
        /*
        * 操作成功
        * */
        descriptor.finish();
        response.dispose();
        return null;
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
            try {
                descriptor.adjustNewLength(length);
            } catch (ResizeConflictException e) {
                /*
                 * 调整失败，则取消当前的下载，并且不能重试
                 * */
                e.printStackTrace();
                response.dispose();
                descriptor.abort();
                throw e;
            }
        }
        // 调整成功
    }
}
