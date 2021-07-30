package ol.rc.client;

import ol.rc.BaseOLRC;
import ol.rc.Image.IImageCompressor;
import ol.rc.Image.ImageCompressorByte;
import ol.rc.Main;
import ol.rc.net.IClient;
import ol.rc.net.IScreenPublisher;
import ol.rc.net.NetObject;
import ol.rc.screen.IScreenReader;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Oleksii Ivanov
 */
public class ScreenPublisherImpl extends BaseOLRC implements IScreenPublisher {
    public static int fileCount = 0;
    private final Object monitorObj = new Object();
    private IClient client;
    private IScreenReader screenReader;
    private IImageCompressor imageCompressor;
    private int fps = 10;
    private int sleepTimeMilliseconds = 100;
    private Thread publishProcess;

    private ScreenPublisherImpl() {

    }

    public ScreenPublisherImpl(IClient client, IScreenReader screenReader) {
        super(ScreenPublisherImpl.class);
        setClient(client);
        imageCompressor = new ImageCompressorByte();
        setScreenReader(screenReader);
        publishProcess = new Thread(createPublishProcess());
        publishProcess.start();
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

}
