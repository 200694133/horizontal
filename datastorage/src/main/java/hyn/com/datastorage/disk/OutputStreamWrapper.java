package hyn.com.datastorage.disk;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/8/22.
 */
public class OutputStreamWrapper extends OutputStream implements Abortable{
    private final IStreamStorage.Editor mEditor;
    private final OutputStream mOutputStream;
    OutputStreamWrapper(IStreamStorage.Editor editor, OutputStream outputStream){
        mEditor = editor;
        mOutputStream = outputStream;
    }

    public void abort()throws IOException{
        mEditor.abort();
        mOutputStream.close();
    }
    public void close() throws IOException {
        mEditor.commit();
        mOutputStream.close();
        mEditor.close();
    }

    public void flush() throws IOException {
        mOutputStream.flush();
    }

    public void write(byte[] buffer) throws IOException {
        mOutputStream.write(buffer);
    }

    public void write(byte[] buffer, int offset, int count) throws IOException {
        mOutputStream.write(buffer,offset,count);
    }

    public void write(int i) throws IOException{
        mOutputStream.write(i);
    }
}
