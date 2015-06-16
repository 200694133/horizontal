package com.hanyanan.http.job.download;

import com.sun.deploy.util.SyncFileAccess;

import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2015/6/16.
 */
public class DiscreteVirtualFileDescriptorProvider implements VirtualFileDescriptorProvider {


    public DiscreteVirtualFileDescriptorProvider(RandomAccessFile accessFile, long size){
//        SyncFileAccess.RandomAccessFileLock
    }






    @Override
    public VirtualFileDescriptor deliveryAndLock() {
        return null;
    }

    @Override
    public void unlock(VirtualFileDescriptor descriptor) throws InterruptedException {

    }

    @Override
    public void completeAndUnlock(VirtualFileDescriptor descriptor) {

    }

    @Override
    public void failedAndUnlock(VirtualFileDescriptor descriptor) {

    }

    @Override
    public void dispose() {

    }
}
