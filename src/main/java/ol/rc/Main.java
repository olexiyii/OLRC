package ol.rc;

import ol.rc.Image.ImageCompressorByte;
import ol.rc.client.ClientImpl;
import ol.rc.client.ScreenPublisherImpl;
import ol.rc.net.IClient;
import ol.rc.net.IDirectingMachine;
import ol.rc.net.IScreenPublisher;
import ol.rc.net.IServer;
import ol.rc.screen.IScreenReader;
import ol.rc.screen.ScreenReaderImpl;
import ol.rc.server.DirectingMachineImpl;
import ol.rc.server.ServerImpl;
import ol.rc.ui.RemoteScreenView;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.Semaphore;
//TODO: zoom image to Frame size
//TODO: fix memory leaks
//TODO: make BlocksImageCompressor - IImageСompressor which will detect changed rectangle areas (blocks) to transfer them.
//TODO: draw changed rectangle areas (blocks) on Graphics, without create Image

public class Main {
    public static final Semaphore SEMAPHORE=new Semaphore(1,true);
    public static int frameCount=10;
    public static void main(String[] args) {
        BaseOLRC.logInfo("Start");
        IDirectingMachine directingMachine=new DirectingMachineImpl();
        int portScreenTranslation = 7777;
        int portControl = 7778;
        IServer server = new ServerImpl(new InetSocketAddress("127.0.0.1", portScreenTranslation), directingMachine);
        IServer serverControl = new ServerImpl(new InetSocketAddress("127.0.0.1", portControl), new DirectingMachineImpl());

        server.start();
        serverControl.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();
        GraphicsDevice gd=screenDevices[0];
        Rectangle bounds=gd.getDefaultConfiguration().getBounds();

        IClient client = null;
        try {
            client=new ClientImpl(new InetSocketAddress("127.0.0.1", portScreenTranslation));
        } catch (IOException e) {
            BaseOLRC.logError(e);
        }
        IClient clientControl = null;
        try {
            clientControl=new ClientImpl(new InetSocketAddress("127.0.0.1", portControl));
        } catch (IOException e) {
            BaseOLRC.logError(e);
        }
        RemoteScreenView remoteScreenView=new RemoteScreenView(server,new ImageCompressorByte(),clientControl);

        remoteScreenView.show();


        IScreenReader screenReader=new ScreenReaderImpl(gd,bounds);

        IScreenPublisher screenPublisher=new ScreenPublisherImpl(client,screenReader,serverControl);
//        long start=System.currentTimeMillis();
//        while (System.currentTimeMillis()-start<10000){
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

    }
}
