package com.hanyanan.http;

/**
 * Created by hanyanan on 2015/6/17.
 */
public class HttpLog {
    public static void d(String tag, String msg){
        System.out.println(tag + " : " +msg);
    }

    public static void w(String tag, String msg){
        System.out.println(tag + " : " +msg);
    }

    public static void e(String tag, String msg){
        System.err.println(tag + " : " +msg);
    }
}
