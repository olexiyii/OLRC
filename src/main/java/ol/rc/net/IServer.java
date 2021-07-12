package ol.rc.net;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface IServer {
    public InetSocketAddress getLocalSocketAddress();
    public void setLocalSocketAddress(InetSocketAddress localSocketAddress) throws IOException;
    public void setDirectingMachine(IDirectingMachine directingMachine);
}
