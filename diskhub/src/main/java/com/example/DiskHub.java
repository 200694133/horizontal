package com.example;

import com.hanyanan.tiny.http.SimpleFileHttpRequestHandler;
import com.hanyanan.tiny.http.TinyGetHttpSever;

import java.io.File;

public class DiskHub {
    public static void main(String []argv) {
        File rootDirect = new File("C:\\");
        TinyGetHttpSever server = new TinyGetHttpSever(38913);
        server.registerRequestHandler("fileBrowse", new FileBrowseRequestHandler(rootDirect));
//        server.registerRequestHandler("getFile", new SimpleFileHttpRequestHandler());
        server.start();
    }
}
