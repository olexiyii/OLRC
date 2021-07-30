package ol.rc.net;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * the {@link IClient} sends info to {@link IServer} by using externalSocket
 */
public interface IClient {
    void send(NetObject obj) throws IOException;

    void setExternalSocket(InetSocketAddress externalSocket) throws IOException;
}
