package ol.rc.net;

import java.net.InetSocketAddress;

/**
 * The {@link IServer} listens <code>localSocketAddress</code> receives <code>Object's</code>
 * and direct they to {@link IDirectingMachine}
 */
public interface IServer {
    InetSocketAddress getLocalSocketAddress();

    /**
     * sets socket for listen
     *
     * @param localSocketAddress - socket for listen
     */
    void setLocalSocketAddress(InetSocketAddress localSocketAddress);

    IDirectingMachine getDirectingMachine();

    void setDirectingMachine(IDirectingMachine directingMachine);
    void start();
}
