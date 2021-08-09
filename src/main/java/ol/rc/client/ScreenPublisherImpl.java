package ol.rc.client;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
import ol.rc.BaseOLRC;
import ol.rc.Image.IImageCompressor;
import ol.rc.Image.ImageCompressorByte;
import ol.rc.Main;
import ol.rc.net.*;
import ol.rc.screen.IScreenReader;
import sun.nio.ch.Net;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * @author Oleksii Ivanov
 */
public class ScreenPublisherImpl extends BaseOLRC implements IScreenPublisher {
    public static int fileCount = 0;
    private final Object monitorObj = new Object();
    private IClient client;
    private IServer server;
    private IScreenReader screenReader;
    private IImageCompressor imageCompressor;
    private int fps = 10;
    private int sleepTimeMilliseconds = 100;
    private Thread publishProcess;

    private WeakReference wsNetObject;

    private ScreenPublisherImpl() {

    }

    public ScreenPublisherImpl(IClient client, IScreenReader screenReader,IServer server) {
        super(ScreenPublisherImpl.class);
        setClient(client);
        this.server=server;
        imageCompressor = new ImageCompressorByte();
        setScreenReader(screenReader);
        publishProcess = new Thread(createPublishProcess());
        publishProcess.start();
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
        try {
            synchronized (monitorObj) {
                client.send(NetObject.createSCREEN_INITIAL(screenReader.getImage()));
            }
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public int getFPS() {
        return fps;
    }

    @Override
    public void setFPS(int fps) {
        this.fps = fps;
        sleepTimeMilliseconds = 1000 / fps;
    }

    private Runnable createPublishProcess() {
        return () -> {
            int errorCountMax = 1;
            while (true) {
                synchronized (monitorObj) {

                    long start = System.currentTimeMillis();

                    NetObject objToSend = getObjectToSend();
                    sendWhithRetry(objToSend, errorCountMax);
                    //wsNetObject.clear();

                    long sleepTimeMillisecondsLeft = sleepTimeMilliseconds - (System.currentTimeMillis() - start);
                    if (sleepTimeMillisecondsLeft > 0) {
                        try {
                            Thread.sleep(sleepTimeMillisecondsLeft);
                        } catch (InterruptedException e) {
                            logError(e);
                        }

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
                try {
                    Main.SEMAPHORE.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                errorCount = 0;
                break;
            } catch (IOException e) {
                e.printStackTrace();
                logError(e);
                errorCount++;
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
            int length=((byte[]) byteArrayDifferencies).length;
            byte[] data=new byte[length];
            System.arraycopy((byte[]) byteArrayDifferencies,0,data,0,length);
        });

        directingMachine.setHandler(NetObject.class, (netObject) -> {
            Main.SEMAPHORE.release();
            System.gc();
            NetObject netObject1 = (NetObject) netObject;
            DataKind dataKind = netObject1.dataKind;
            switch (dataKind) {
                case MOUSE:
                    //screenReader.receiveMouseEvent((MouseEvent) netObject1.data);
                    break;
                case KEY:
                    screenReader.receiveKey((KeyEvent) netObject1.data);
                    break;
                case SCREEN_INITIAL:
                    directingMachine.direct(netObject1.data);
                    netObject1=null;
                    break;
                case SCREEN_DIFFERENCES:
//                    byte[] data= (byte[]) netObject1.data;
//                    byte[] data1=new byte[data.length];
                    directingMachine.direct(netObject1.data);
                    netObject1=null;
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
                    System.out.println("dataKind:" + dataKind);
                    break;
            }
        });

    }
//    void mouseReceive(MouseEvent mouseEvent){
//        screenReader.receiveMouseEvent(mouseEvent);
//    }
//    void keyReceive(KeyEvent keyEvent){
//        screenReader.receiveKey(keyEvent);
//    }

}
