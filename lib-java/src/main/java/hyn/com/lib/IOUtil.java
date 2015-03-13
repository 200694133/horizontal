package hyn.com.lib;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

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
}
