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

public class Main {
    public static final Semaphore SEMAPHORE=new Semaphore(1,true);
    public static void main(String[] args) {
        BaseOLRC.logInfo("Start");
        IDirectingMachine directingMachine=new DirectingMachineImpl();
        int port = 7777;
        IServer server = new ServerImpl(new InetSocketAddress("127.0.0.1", port), directingMachine);

        RemoteScreenView remoteScreenView=new RemoteScreenView(server,new ImageCompressorByte());
        server.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();
        GraphicsDevice gd=screenDevices[0];
        Rectangle bounds=gd.getDefaultConfiguration().getBounds();

        remoteScreenView.show();

        IClient client = null;
        try {
            client=new ClientImpl(new InetSocketAddress("127.0.0.1", port));
        } catch (IOException e) {
            BaseOLRC.logError(e);
        }
        IScreenReader screenReader=new ScreenReaderImpl(gd,bounds);

        IScreenPublisher screenPublisher=new ScreenPublisherImpl(client,screenReader);
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
