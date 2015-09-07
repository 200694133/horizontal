package com.hanyanan.tiny.http;


/**
 * Created by hanyanan on 2014/12/31.
 */
public class Main {
    public static void main(String[] args) {
        TinyGetHttpSever server = new TinyGetHttpSever(8569);
        server.registerRequestHandler("file", new SimpleFileHttpRequestHandler());
        server.start();
    }
}
