package hyn.com.datastorage.disk;

import java.io.File;

/**
 * Created by hanyanan on 2014/8/22.
 */
public class UnlimitedDiskStorage extends BasicDiskStorage {
    private UnlimitedDiskStorage(File rootDirectory) {
        super(rootDirectory);
    }

    public synchronized static UnlimitedDiskStorage open(File rootDirectory){
        return new UnlimitedDiskStorage(rootDirectory);
    }
}
