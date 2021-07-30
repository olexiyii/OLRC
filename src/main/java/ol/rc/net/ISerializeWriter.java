package ol.rc.net;

import java.io.OutputStream;

public interface ISerializeWriter<O extends OutputStream,T> {
    void write(O outputStream,T obj);
}
