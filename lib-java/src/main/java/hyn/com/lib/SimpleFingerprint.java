package hyn.com.lib;

/**
 * Created by hanyanan on 2015/6/3.
 */
public class SimpleFingerprint implements Fingerprint {
    @Override
    public String fingerprint() {
        return this.toString();
    }
}
