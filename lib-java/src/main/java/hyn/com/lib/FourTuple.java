package hyn.com.lib;

/**
 * Created by hanyanan on 2015/3/17.
 */
public class FourTuple<T1, T2, T3, T4> extends ThreeTuple {
    public final T4 fourthValue;
    public FourTuple(T1 value1, T2 value2, T3 value3, T4 value4) {
        super(value1, value2,value3);
        fourthValue = value4;
    }
}
