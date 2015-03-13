package hyn.com.lib;

import java.util.List;
import java.util.Set;

/**
 * Created by hanyanan on 2015/2/13.
 */
public class TwoTuple<T1, T2> {
    public final T1 firstValue;
    public final T2 secondValue;

    public TwoTuple(T1 value1, T2 value2){
        firstValue = value1;
        secondValue = value2;List set;
    }

    public static <T1, T2> TwoTuple<T1, T2>tuple(T1 value1, T2 value2){
        return new TwoTuple<T1,T2>(value1,value2);
    }
}
