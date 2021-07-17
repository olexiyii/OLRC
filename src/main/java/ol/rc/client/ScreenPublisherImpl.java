package ol.rc.client;

import ol.rc.BaseOLRC;
import ol.rc.Image.IImageСompressor;
import ol.rc.Image.ImageCompressorByte;
import ol.rc.net.IClient;
import ol.rc.net.IScreenPublisher;
import ol.rc.screen.IScreenReader;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Oleksii Ivanov
 */
public class ScreenPublisherImpl extends BaseOLRC implements IScreenPublisher {
    private IClient client;
    private IScreenReader screenReader;
    private IImageСompressor imageСompressor;
    private int fps = 10;
    private int sleepTimeMilliseconds = 100;
    private Thread publishProcess;
    private Object semafore = new Object();

    private ScreenPublisherImpl(){

    }

    public ScreenPublisherImpl(IClient client,IScreenReader screenReader) {
        super(ScreenPublisherImpl.class);
        setClient(client);
        setScreenReader(screenReader);
        imageСompressor = new ImageCompressorByte();
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
        imageСompressor.setImageSettings(screenReader.getBounds());
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

            int errorCountMax = 10;
            while (true) {
                synchronized (semafore) {

                    long start = System.currentTimeMillis();
                    Object objToSend;
                    BufferedImage screenImage = screenReader.getImage();
                    if (screenReader.isNewSettings()){
                        objToSend=screenImage;
                        screenReader.resetNewSettings();
                        imageСompressor.setImageSettings(screenReader.getBounds());
                    }else {
                        objToSend=imageСompressor.compress(screenImage);
                    }

//                    byte[] imgData = imageСompressor.compress(screenImage);
                    int errorCount = 0;
                    while (errorCount < errorCountMax) {
                        try {
                            client.send(objToSend);
                            errorCount = 0;
                            break;
                        } catch (IOException e) {
                            logError(e);
                            errorCount++;
                        }
                    }

                    long sleepTimeMillisecondsLeft = sleepTimeMilliseconds - (System.currentTimeMillis() - start);
                    if (sleepTimeMillisecondsLeft > 0) {
                        try {
                            Thread.sleep(sleepTimeMilliseconds);
                        } catch (InterruptedException e) {
                            logError(e);
                        }
                    }
                }
            }
        };
    }

}
