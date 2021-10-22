package ol.rc.server;

import ol.rc.BaseOLRC;
import ol.rc.net.IDirectingMachine;
import ol.rc.net.IServer;
import ol.rc.net.NetObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

public class ServerImpl extends BaseOLRC implements IServer {

    private ServerSocketChannel serverSocketChannel;
    private InetSocketAddress localSocketAddress;
    private Thread receiveThread;
    private IDirectingMachine directingMachine;

    public ServerImpl() {
        super(ServerImpl.class);
    }

    public ServerImpl(IDirectingMachine directingMachine) {
        this();
        setDirectingMachine(directingMachine);
    }

    public ServerImpl(InetSocketAddress localSocketAddress, IDirectingMachine directingMachine) {
        this(directingMachine);
        setLocalSocketAddress(localSocketAddress);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return localSocketAddress;
    }


    @Override
    public void setLocalSocketAddress(InetSocketAddress localSocketAddress) {
        stop();
        this.localSocketAddress = localSocketAddress;
        start();
    }

    @Override
    public IDirectingMachine getDirectingMachine() {
        return directingMachine;
    }

    @Override
    public void setDirectingMachine(IDirectingMachine directingMachine) {
        this.directingMachine = directingMachine;
    }

    @Override
    public void start() {
        if (receiveThread ==null) {
            receiveThread = new Thread(createListenerProcess());
            receiveThread.setDaemon(true);
            receiveThread.start();
        }

    }

    @Override
    public void stop() {
        closeReceiveProcess();
    }

    private void closeReceiveProcess() {
        if (receiveThread==null){
            return;
        }
        try {
            receiveThread.interrupt();
        } catch (Exception e) {
            error(e);
        }
        receiveThread = null;
    }

    private Runnable createListenerProcess() {
        return () -> {

            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(new InetSocketAddress(localSocketAddress.getPort()));
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
                BaseOLRC.logInfo("SERVER objectInputStream error" );
                error(e);
                return;
            }
            while (true) {

                try {
                    WeakReference wr=new WeakReference(objectInputStream.readObject());
                    directingMachine.direct(wr.get());
                    wr.clear();
                } catch (IOException | ClassNotFoundException e) {
                    BaseOLRC.logInfo("SERVER objectInputStream readObject error" );
                    error(e);
                    break;
                }
            }
        };
    }

    private void updateServerSocket() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(localSocketAddress.getPort()));

        closeReceiveProcess();

        receiveThread = new Thread(createListenerProcess());
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
