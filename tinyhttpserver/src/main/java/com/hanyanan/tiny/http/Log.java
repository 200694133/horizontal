package com.hanyanan.tiny.http;

public class Log {
	public synchronized static void d(String s){
        System.err.println("Amazing"+(s+Thread.currentThread().getId()));
	}
	public synchronized static void e(String s){
        System.err.println("Amazing"+(s+Thread.currentThread().getId()));
	}
}
