package com.example;

import com.example.model.FileListBean;
import com.google.gson.Gson;
import com.hanyanan.tiny.http.Copier;
import com.hanyanan.tiny.http.DefaultCopier;
import com.hanyanan.tiny.http.HttpRequestHandler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hyn.com.lib.IOUtil;

/**
 * Created by hanyanan on 2015/9/7.
 */
public class FileBrowseRequestHandler implements HttpRequestHandler {
    private final File rootDirectory;

    public FileBrowseRequestHandler(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public Copier getCopier() {
        return new DefaultCopier();
    }


    public FileListBean handle(String path) {
        File targetFile = new File(path);
        FileListBean result = new FileListBean();
        if (targetFile.exists() && targetFile.isDirectory()) {
            result.code = 0;
            File[] files = targetFile.listFiles();
            if (null != files) {
                List<FileListBean.FileBean> FileBeans = new ArrayList<FileListBean.FileBean>();
                for (File file : files) {
                    if (file.getPath().endsWith(".") || file.getPath().endsWith("..")) {
                        continue;
                    }
                    FileListBean.FileBean fileBean = new FileListBean.FileBean();
                    fileBean.isDirectory = file.isDirectory();
                    fileBean.name = file.getName();
                    fileBean.absPath = file.getAbsolutePath();
                    fileBean.lastModifyTime = file.lastModified();
                    fileBean.size = file.length();
                    FileBeans.add(fileBean);
                }
                result.fileList = FileBeans.toArray(new FileListBean.FileBean[0]);
            }
        } else {
            result.code = 1;
            result.msg = "当前文件不是目录或者不存在!";
        }
        return result;
    }

    private Gson gson = new Gson();

    @Override
    public HandlerResult handle(String path, Map<String, String> requestParam, Map<String, String> requestHeaders) throws IOException {
        String absPath = URLDecoder.decode(requestParam.get("path"), "utf-8");
        FileListBean result = handle(absPath);
        HandlerResult handlerResult = new HandlerResult();
        byte[] data = URLEncoder.encode(gson.toJson(result)).getBytes();
        handlerResult.size = data.length;
        handlerResult.inputStream = IOUtil.bytesToInputStream(data);
        handlerResult.responseCode = 200;
        return handlerResult;
    }
}
