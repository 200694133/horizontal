package hyn.com.datastorage.db;

/**
 * Created by hanyanan on 2015/3/31.
 */
public interface DeadlineStorage {
    public void clearTrash();

    public boolean isExpired(final String key);

    public boolean enableExpireRestriction();
}
