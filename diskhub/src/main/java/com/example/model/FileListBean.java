package com.example.model;

/**
 * Created by hanyanan on 2015/9/7.
 */
public class FileListBean {
    public int code;
    public String msg;
    public FileBean [] fileList;

    public static class FileBean {
        public String name;
        public String mimeType;
        public String thumbnailUrl;
        public String absPath;
        public long size;
        public long lastModifyTime;
        public boolean isDirectory;
    }
}
