package hyn.com.datastorage.disk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;

import hyn.com.lib.android.logging.Log;

/**
 * Created by hanyanan on 2014/7/28.
 */
class FixSizeDiskStorageImpl extends FlexibleDiskStorageImpl {
    private static final String TAG = "FixSizeDiskStorage";
    private final static long DEFAULT_MAX_SIZE = 1024 * 1024 * 20;//10M

    final long mMaxSize;
    long mCurrSize = 0;

    private FixSizeDiskStorageImpl(File directory, int appVersion, long size) {
        super(directory, appVersion);
        mMaxSize = size;
    }

    private FixSizeDiskStorageImpl(File directory, int appVersion) {
        this(directory, appVersion, DEFAULT_MAX_SIZE);
    }

    /**
     * Opens the cache in {@code directory}, creating a cache if none exists
     * there.
     *
     * @param directory a writable directory
     * @throws java.io.IOException if reading or writing the cache directory fails
     */
    public static FixSizeDiskStorageImpl open(File directory, int appVersion,long size)
            throws IOException {
        // If a bkp file exists, use it instead.
        File backupFile = new File(directory, JOURNAL_FILE_BACKUP);
        if (backupFile.exists()) {
            File journalFile = new File(directory, JOURNAL_FILE);
            // If journal file also exists just delete backup file.
            if (journalFile.exists()) {
                backupFile.delete();
            } else {
                renameTo(backupFile, journalFile, false);
            }
        }

        // Prefer to pick up where we left off.
        FixSizeDiskStorageImpl cache = new FixSizeDiskStorageImpl(directory, appVersion, size);
        if (cache.journalFile.exists()) {
            try {
                cache.journalWriter = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(cache.journalFile, true),
                                DiskCharset.ASCII_CHARSET));

                cache.readJournal();
                cache.processJournal();

                cache.trimToSizeIfNeed();
                return cache;
            } catch (IOException journalIsCorrupt) {
                Log.e("Disk Lru","DiskLruCache " + directory + " is corrupt: "
                        + journalIsCorrupt.getMessage() + ", removing");
                cache.delete();
            }
        }

        // Create a new empty cache.
        directory.mkdirs();
        cache = new FixSizeDiskStorageImpl(directory, appVersion,size);
        cache.rebuildJournal();
        cache.trimToSizeIfNeed();
        return cache;
    }

    protected void onEntryChanged(String key, long oldLength, long currLength){
        Log.d(TAG, "onEntryChanged old="+oldLength+", new="+currLength);
        mCurrSize = mCurrSize - oldLength + currLength;
        Log.d(TAG, "onEntryChanged Current size "+mCurrSize);
        trimToSizeIfNeed();
    }
    protected void onEntryRemoved(String key, long length){
        Log.d(TAG, "onEntryRemoved old="+length);
        mCurrSize = mCurrSize - length;
        Log.d(TAG, "onEntryRemoved Current size "+mCurrSize);
        trimToSizeIfNeed();
    }
    protected void onEntryAdded(String key, long length){
        Log.d(TAG, "onEntryAdded addSize="+length);
        mCurrSize = mCurrSize + length;
        Log.d(TAG, "onEntryAdded Current size "+mCurrSize);
        trimToSizeIfNeed();
    }
    protected void onEntryClear(){
        mCurrSize = 0;
    }

    public long getCurrSize(){
        return mCurrSize;
    }
    public long getMaxSize(){
        return mMaxSize;
    }
    private void trimToSizeIfNeed(){
        synchronized (this){
            if(mCurrSize > mMaxSize){
                Log.d(TAG, "mMaxSize "+mMaxSize);
                trimToSize();
            }
        }
    }
    private void trimToSize(){
        Iterator<Map.Entry<String, Entry>> iterator = lruEntries.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Entry> entry = iterator.next();
            Entry e = entry.getValue();

            try {
                remove(e.getKey());
                if(null != mOverFlowRemoveListener){
                    mOverFlowRemoveListener.remove(e.getKey(), 0, e.getLength());
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (Log.enableDebug()) {
                Log.v("Disk Lru","trimToSize() remove "+e.getKey());
            }
            if (mCurrSize <= mMaxSize) {
                break;
            }
        }
    }
}
