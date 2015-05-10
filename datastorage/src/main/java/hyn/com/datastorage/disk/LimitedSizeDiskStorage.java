package hyn.com.datastorage.disk;

import java.io.File;
import java.io.IOException;

/**
 * Created by hanyanan on 2014/8/22.
 */
public class LimitedSizeDiskStorage extends BasicDiskStorage {
    private LimitedSizeDiskStorage(IStreamStorage streamStorage) {
        super(streamStorage);
    }

    public synchronized static LimitedSizeDiskStorage open(File rootDirector){
        return open(rootDirector, 50 * 1024 * 1024);//50M
    }

    public synchronized static LimitedSizeDiskStorage open(File rootDirector, long size){
        try {
            FixSizeDiskStorageImpl fixSizeDiskStorage = FixSizeDiskStorageImpl.open(rootDirector,VERSION,size);
            return new LimitedSizeDiskStorage(fixSizeDiskStorage);
        } catch (IOException e) {
            return null;
        }
    }
}
