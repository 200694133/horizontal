package com.hanyanan.http.job.download;

import hyn.com.lib.Disposeable;

/**
 * Created by hanyanan on 2015/6/16.
 */
public interface VirtualFileDescriptorProvider extends Disposeable {
    VirtualFileDescriptor deliveryAndLock();

    void unlock(VirtualFileDescriptor descriptor) throws InterruptedException;

    void completeAndUnlock(VirtualFileDescriptor descriptor);

    void failedAndUnlock(VirtualFileDescriptor descriptor);
}
