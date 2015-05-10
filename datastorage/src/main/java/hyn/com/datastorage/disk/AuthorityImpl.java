package hyn.com.datastorage.disk;

/**
 * Created by hanyanan on 2014/10/22.
 */
public class AuthorityImpl implements Authority {
    @Override
    public boolean isValid(long expireTime) {
        if(expireTime< 0 || System.currentTimeMillis() <= expireTime) return true;
        return false;
    }
}
