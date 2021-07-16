package ol.rc.client;

import ol.rc.BaseOLRC;
import ol.rc.net.IClient;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ClientImpl extends BaseOLRC implements IClient {
    private ObjectOutputStream outputStream;
    private InetSocketAddress externalSocket;
    private SocketChannel socketChannelWrite;

    private ClientImpl() {
        super();
    }

    public ClientImpl(InetSocketAddress localSocket) throws IOException {
        super(ClientImpl.class);
        setExternalSocket(localSocket);
        logger.info("Client created");
    }

    @Override
    public void send(Object obj) throws IOException {
        outputStream.writeObject(obj);
        outputStream.flush();
    }

    private void updateExternalSocket() throws IOException {
        try {
            socketChannelWrite = SocketChannel.open();

            socketChannelWrite.socket().connect(new InetSocketAddress(externalSocket.getAddress(), externalSocket.getPort()));
        } catch (IOException e) {
            error(e);
        }
        outputStream = new ObjectOutputStream(socketChannelWrite.socket().getOutputStream());
    }


    @Override
    public void setExternalSocket(InetSocketAddress externalSocket) throws IOException {
        this.externalSocket = externalSocket;
        updateExternalSocket();
    }
}
