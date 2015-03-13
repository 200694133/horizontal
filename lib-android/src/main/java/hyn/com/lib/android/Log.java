package hyn.com.lib.android;

import java.util.Objects;

/**
 * Created by hanyanan on 2015/3/4.
 */
public class Log {
    public static final String TAG = "horizontal";

    public static Log tag(String tag){
        return new Log();
    }


    public static void d(String log, Object ... objects){
        android.util.Log.d(TAG, String.format(log,objects));
    }
    public static void d(String log){
        android.util.Log.d(TAG, log);
    }

//    public static void d(String tag,String log){
//        android.util.Log.d(tag, log);
//    }
//    public static void d(String tag,String log, Object ... objects){
//        android.util.Log.d(tag, String.format(log,objects));
//    }





    public static void e(String log, Object ... objects){
        android.util.Log.e(TAG, String.format(log,objects));
    }
    public static void e(String log){
        android.util.Log.e(TAG, log);
    }

//    public static void e(String tag,String log){
//        android.util.Log.e(tag, log);
//    }
//    public static void e(String tag,String log, Object ... objects){
//        android.util.Log.e(tag, String.format(log,objects));
//    }


    public static void i(String log, Object ... objects){
        android.util.Log.i(TAG, String.format(log,objects));
    }
    public static void i(String log){
        android.util.Log.i(TAG, log);
    }

//    public static void i(String tag,String log){
//        android.util.Log.i(tag, log);
//    }
//    public static void i(String tag,String log, Object ... objects){
//        android.util.Log.i(tag, String.format(log,objects));
//    }

    public static void w(String log, Object ... objects){
        android.util.Log.w(TAG, String.format(log,objects));
    }
    public static void w(String log){
        android.util.Log.w(TAG, log);
    }

//    public static void w(String tag,String log){
//        android.util.Log.w(tag, log);
//    }
//    public static void w(String tag,String log, Object ... objects){
//        android.util.Log.w(tag, String.format(log,objects));
//    }


    public static void v(String log, Object ... objects){
        android.util.Log.v(TAG, String.format(log,objects));
    }
    public static void v(String log){
        android.util.Log.v(TAG, log);
    }
//
//    public static void v(String tag,String log){
//        android.util.Log.v(tag, log);
//    }
//    public static void v(String tag,String log, Object ... objects){
//        android.util.Log.v(tag, String.format(log,objects));
//    }
}
