package hyn.com.datastorage.db;

/**
 * Created by hanyanan on 2015/4/4.
 */
public interface OnOverFlowListener {
    /**
     * It's not support any effective way to get the previous value. So user cannot relay on this
     * callback to try to do the
     * @param key the specify key will be deleted from storage.
     */
    public void onOverFlow(final String key);
}
