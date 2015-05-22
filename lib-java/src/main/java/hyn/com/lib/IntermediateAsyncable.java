package hyn.com.lib;

/**
 * Created by hanyanan on 2015/5/9.
 */
public interface IntermediateAsyncable<P,I,R> {
    R run(P param) throws Throwable;

    void intermediate(I intermediate);
}
