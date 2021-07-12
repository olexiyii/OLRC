package ol.rc.net;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface IClient {
    public void send(Object obj) throws IOException;
    public void setExternalSocket(InetSocketAddress localSocket) throws IOException;
}
