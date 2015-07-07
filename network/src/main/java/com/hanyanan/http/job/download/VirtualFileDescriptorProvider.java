package com.hanyanan.http.job.download;

import java.util.List;

import hyn.com.lib.Disposeable;

/**
 * Created by hanyanan on 2015/6/16.
 */
public interface VirtualFileDescriptorProvider {
    VirtualFileDescriptor deliveryAndLock();

    /**
     * Force close current provider, which means that any {@link VirtualFileDescriptor} will be dispose and cannot
     * save any state, data has downloaded will be disposed.
     */
    void close();

    boolean isClosed();

//    /**
//     * delay close current provider, it will not closed until all descriptors has been closed.
//     */
//    void delayClose();

//    List<VirtualFileDescriptor> getVirtualFileDescriptors();
}
