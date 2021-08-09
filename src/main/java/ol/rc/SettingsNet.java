package ol.rc;

import ol.rc.net.IClient;
import ol.rc.net.IServer;
import ol.rc.utils.JSTUNClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsNet extends BaseOLRC {
    JSTUNClient.ConnectionData connectionData;

    private IClient client;
    private IServer server;
    private OlAppType appType;
    private InetSocketAddress localAddress;
    private InetSocketAddress externalAddress;
    private int fps;
    private InetSocketAddress remoteAddress;

    public SettingsNet(IClient client,IServer server){
        this.client=client;
        this.server=server;
    }

    public OlAppType getAppType() {
        return appType;
    }

    public void setAppType(OlAppType appType) {
        this.appType = appType;
    }

    private List<InetSocketAddress> getSTUNServers() {

        InputStream is = getClass().getClassLoader().getResourceAsStream("stunServers.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        int defaultSTUNPort = 3478;

        return br.lines()
                .filter(str -> !(str.startsWith("#") || str.length() < 10))
                .map(str -> {
                    String[] addressPort = str.split(":");
                    int port = addressPort.length == 2 ? Integer.parseInt(addressPort[1]) : defaultSTUNPort;
                    return new InetSocketAddress(addressPort[0], port);
                }).collect(Collectors.toList());
    }

    private JSTUNClient.ConnectionData getConnectionData() {
        if (connectionData == null) {
            connectionData = JSTUNClient.getExtermalConnectionData(getSTUNServers());
        }
        return connectionData;
    }

    public InetSocketAddress getLocalAddress() {
        if (localAddress == null) {

            setLocalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getLocalAddress(), getConnectionData().datagramSocket.getLocalPort()));
            setExternalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getInetAddress(), getConnectionData().datagramSocket.getPort()));
        }
        return localAddress;
    }
    public void resetLocalAddress(){
        localAddress = null;
    }
    public void resetExternalAddress(){
        externalAddress = null;
    }

    private void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
        server.setLocalSocketAddress(this.localAddress);
    }

    public InetSocketAddress getExternalAddress() {
        if (externalAddress == null) {

            setLocalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getLocalAddress(), getConnectionData().datagramSocket.getLocalPort()));
            setExternalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getInetAddress(), getConnectionData().datagramSocket.getPort()));
        }
        return externalAddress;
    }

    private void setExternalAddress(InetSocketAddress externalAddress) {
        this.externalAddress = externalAddress;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        try {
            client.setExternalSocket(this.remoteAddress);
        } catch (IOException e) {
            logError(e);
        }
    }

    public IClient getClient() {
        return client;
    }

    public IServer getServer() {
        return server;
    }

}
