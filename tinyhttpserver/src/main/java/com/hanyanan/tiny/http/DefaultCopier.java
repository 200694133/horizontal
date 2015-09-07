package com.hanyanan.tiny.http;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/12/31.
 */
public class DefaultCopier implements Copier {
    @Override
    public void copy(InputStream inputStream, OutputStream outputStream, long size, String encode)
            throws IOException {
        byte[] buff = new byte[1024 * 64];
        int read = buff.length;
        read = size > read ? read : (int) size;
        long t = System.currentTimeMillis();
        while ((read = inputStream.read(buff, 0, read)) > 0) {
            System.err.println("Read "+ read + " cost "+(System.currentTimeMillis()-t));
            t = System.currentTimeMillis();
            outputStream.write(buff, 0, read);
            System.err.println("Write "+ read + " cost "+(System.currentTimeMillis()-t));
            t = System.currentTimeMillis();

//            Log.d("Write " + read + " byte");
            size -= read;
            read = size > read ? read : (int) size;
//            try {
//                Thread.sleep(30);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }
}
