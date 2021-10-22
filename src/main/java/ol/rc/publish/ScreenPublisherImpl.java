package ol.rc.publish;

import com.aparapi.device.Device;
import ol.rc.BaseOLRC;
import ol.rc.Image.IImageCompressor;
import ol.rc.Image.ImageCompressorByte;
import ol.rc.net.*;
import ol.rc.screen.IScreenReader;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;

/**
 * @author Oleksii Ivanov
 */
public class ScreenPublisherImpl extends BaseOLRC implements IScreenPublisher {
    public static int count = 0;
    private final Object monitorObj = new Object();
    private IClient client;
    private IServer server;
    private IScreenReader screenReader;
    private IImageCompressor imageCompressor;
    private int fps = 20;
    private int sleepTimeMilliseconds = 100;
    private Thread publishProcess;

    private ScreenPublisherImpl() {

    }

    public ScreenPublisherImpl(IClient client, IScreenReader screenReader, IServer server, Device device) {
        this(client,screenReader,server);
        imageCompressor.setDeviceForParallelCalc(device);
    }

    public ScreenPublisherImpl(IClient client, IScreenReader screenReader,IServer server) {
        super(ScreenPublisherImpl.class);
        setClient(client);
        this.server=server;
        imageCompressor = new ImageCompressorByte();
        setScreenReader(screenReader);
        publishProcess = new Thread(createPublishProcess());
        //publishProcess.start();
        createDirectingMachine();
        //this.server.start();
    }

    @Override
    public IClient getClient() {
        return client;
    }

    @Override
    public void setClient(IClient client) {
        this.client = client;
    }

    @Override
    public IScreenReader getScreenReader() {
        return screenReader;
    }

    @Override
    public void setScreenReader(IScreenReader screenReader) {
        this.screenReader = screenReader;
        imageCompressor.setImageSettings(screenReader.getBounds());
    }

    @Override
    public int getFPS() {
        return fps;
    }

    @Override
    public void start() {
        if (publishProcess==null){
            publishProcess = new Thread(createPublishProcess());
        }
        if (!publishProcess.isAlive()){
            publishProcess.start();
        }
        server.start();
    }

    @Override
    public void stop() {
        publishProcess.interrupt();
        publishProcess=null;
    }

    @Override
    public void setFPS(int fps) {
        this.fps = fps;
        sleepTimeMilliseconds = 1000 / fps;
    }

    private Runnable createPublishProcess() {
        return () -> {
            int errorCountMax = 1;
            long start = System.currentTimeMillis();
            while (true) {
                System.gc();

                WeakReference<NetObject> wr=new WeakReference<>(getObjectToSend());
                sendWhithRetry(wr.get(), errorCountMax);
                wr.clear();
                wr=null;
                long sleepTimeMillisecondsLeft = sleepTimeMilliseconds - (System.currentTimeMillis() - start);
                start = System.currentTimeMillis();
                if (sleepTimeMillisecondsLeft > 0) {
                    try {
                        Thread.sleep(sleepTimeMillisecondsLeft);
                    } catch (InterruptedException e) {
                        logError(e);
                    }

                }
            }
        };
    }

    private void sendWhithRetry(NetObject objToSend, int errorCountMax) {
        int errorCount = 0;

        while (errorCount < errorCountMax) {
            try {
                client.send(objToSend);

                errorCount = 0;
                break;
            } catch (IOException e) {
                e.printStackTrace();
                logError(e);
                errorCount++;
                logInfo("sendWhithRetry errorCount:"+errorCount);
            }
        }
    }


    private NetObject getObjectToSend() {
        NetObject objToSend;
        BufferedImage screenImage = screenReader.getImage();

        if (screenReader.isNewSettings()) {
            objToSend = NetObject.createSCREEN_INITIAL(screenImage);
            screenReader.resetNewSettings();
            imageCompressor.setResultImage(screenImage);
        } else {
            objToSend = NetObject.createSCREEN_DIFFERENCES(imageCompressor.compress(screenImage));
        }
        return objToSend;
    }

    private void createDirectingMachine() {
        IDirectingMachine directingMachine=server.getDirectingMachine();

        directingMachine.setHandler(byte[].class, (byteArrayDifferencies) -> {
//            int length=((byte[]) byteArrayDifferencies).length;
//            byte[] data=new byte[length];
//            System.arraycopy((byte[]) byteArrayDifferencies,0,data,0,length);
        });

        directingMachine.setHandler(NetObject.class,null);
        directingMachine.setHandler(InetSocketAddress.class,null);

        directingMachine.setHandler(NetObject.class, (netObject) -> {
            System.gc();
            NetObject netObject1 = (NetObject) netObject;
            DataKind dataKind = netObject1.dataKind;
            switch (dataKind) {
                case MOUSE:
                    screenReader.receiveMouseEvent((MouseEvent) netObject1.data);
                    break;
                case KEY:
                    screenReader.receiveKey((KeyEvent) netObject1.data);
                    break;
                case SCREEN_INITIAL:
                    if(netObject1.data==null){
                        screenReader.setNewSettings();
                    }
                    break;
                case SCREEN_DIFFERENCES:
                    break;
                case FILE_FINISH:
                    //TODO make file transfer
                    break;
                case FILE_START_NAME:
                    //TODO make file transfer
                    break;
                case FILE_DATA:
                    //TODO make file transfer
                    break;
                case OBJECT:
                    break;
                default:
                    //directingMachine.direct(netObject1.data);
                    System.out.println("dataKind:" + dataKind);
                    break;
            }
            netObject1=null;
        });

    }

}
