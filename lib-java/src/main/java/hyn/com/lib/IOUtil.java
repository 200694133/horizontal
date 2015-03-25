package hyn.com.lib;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class IOUtil {
    public static void renameTo(File from, File to, boolean deleteDestination) throws IOException {
        if (deleteDestination) {
            deleteIfExists(to);
        }
        if (!from.renameTo(to)) {
            throw new IOException();
        }
    }
    public static void safeRenameTo(File from, File to, boolean deleteDestination) {
        try {
            renameTo(from, to, deleteDestination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void safeDeleteIfExists(File file){
        try {
            deleteIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("delete failed");
        }
    }
    public static void safeClose(Closeable closeable){
        if(null == closeable) return ;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // parse byte[] by inputStream.将byte[]转换成InputStream
    public InputStream Byte2InputStream(byte[] b) {
        ByteArrayInputStream res = new ByteArrayInputStream(b);
        return res;
    }

    // change the inp将InputStream转换成byte[]
    public byte[] InputStream2Bytes(InputStream is) {
        String str = "";
        byte[] readByte = new byte[1024];
        int readCount = -1;
        try {
            while ((readCount = is.read(readByte, 0, 1024)) != -1) {
                str += new String(readByte).trim();
            }
            return str.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
