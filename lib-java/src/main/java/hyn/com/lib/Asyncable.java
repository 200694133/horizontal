package hyn.com.lib;

/**
 * Created by hanyanan on 2015/5/9.
 */
public interface Asyncable<P,R> {
    R run(P param);
}
