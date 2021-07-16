package ol.rc.server;

import ol.rc.BaseOLRC;
import ol.rc.net.IDirectingMachine;
import ol.rc.net.IServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerImpl extends BaseOLRC implements IServer {
    private ServerSocketChannel serverSocketChannel;
    private InetSocketAddress localSocketAddress;
    private Thread receiveThread;
    private IDirectingMachine directingMachine;

    public ServerImpl(InetSocketAddress localSocketAddress, IDirectingMachine directingMachine) {
        super(ServerImpl.class);
        setDirectingMachine(directingMachine);
        setLocalSocketAddress(localSocketAddress);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return localSocketAddress;
    }

    @Override
    public void setLocalSocketAddress(InetSocketAddress localSocketAddress) {
        this.localSocketAddress = localSocketAddress;
        try {
            updateServerSocket();
        } catch (Exception e) {
            error(e);
        }
    }

    @Override
    public IDirectingMachine getDirectingMachine() {
        return directingMachine;
    }

    @Override
    public void setDirectingMachine(IDirectingMachine directingMachine) {
        this.directingMachine = directingMachine;
    }

    private void closeReceiveProcess() {
        try {
            receiveThread.interrupt();
        } catch (Exception e) {
            error(e);
        }
        receiveThread = null;
    }

    private Runnable createListenerProcess() {
        return () -> {
            int count = 0;

            try {
                serverSocketChannel.configureBlocking(true);
            } catch (IOException e) {
                BaseOLRC.logError(e);
            }
            ObjectInputStream objectInputStream;

            SocketChannel socketChannel;
            try {
                socketChannel = serverSocketChannel.accept();
                objectInputStream = new ObjectInputStream(socketChannel.socket().getInputStream());
            } catch (IOException e) {
                error(e);
                return;
            }
            while (true) {

                try {
                    Object obj = objectInputStream.readObject();
                    directingMachine.direct(obj);
                } catch (IOException | ClassNotFoundException e) {
                    error(e);
                    break;
                }
                count++;
            }
        };
    }

    private void updateServerSocket() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(localSocketAddress.getPort()));

        closeReceiveProcess();

        receiveThread = new Thread(createListenerProcess());
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        receiveThread.interrupt();
    }

    public void setLocalSocketAddress(InetAddress destIaddress, int destPort) {
        setLocalSocketAddress(new InetSocketAddress(destIaddress, destPort));
    }


}
