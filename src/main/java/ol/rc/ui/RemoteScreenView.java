package ol.rc.ui;

import ol.rc.BaseOLRC;
import ol.rc.Image.IImageCompressor;
import ol.rc.Main;
import ol.rc.net.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class RemoteScreenView extends BaseOLRC {
    Graphics imageGraphics;
    private IServer server;
    private IClient client;
    private IDirectingMachine directingMachine;
    private IImageCompressor imageCompressor;
    private JFrame frame;
    private JPanel panel;
    private JComponent imageComponent;
    private ImageObserver imageObserver;


    public RemoteScreenView(IServer server, IImageCompressor imageCompressor,IClient client) {
        super(RemoteScreenView.class);
        this.server = server;
        this.client = client;
        directingMachine = server.getDirectingMachine();
        this.imageCompressor = imageCompressor;
        createDirectingMachine();
        createUI();
    }

    Graphics getImageGraphics() {
//        if (imageGraphics == null) {
//            imageGraphics = imagePanel.getGraphics();
//        }
        return getImageComponent().getGraphics();
    }
    JComponent createImageComponent(){
        JPanel result=new JPanel();
        MouseInputAdapter mouseInputAdapter=new OlMouseInputAdapter(client);
        result.addMouseListener(mouseInputAdapter);
        OlKeyAdapter keyAdapter=new OlKeyAdapter(client);
        result.addKeyListener(keyAdapter);
        result.setFocusable(true);
        return  result;
    }
    JComponent getImageComponent() {
        if (imageComponent == null) {
            imageComponent = createImageComponent();
        }
        return imageComponent;
    }

    private void createDirectingMachine() {


        directingMachine.setHandler(BufferedImage.class, (bufferedImage) -> {
            BufferedImage bufferedImage1=((BufferedImage)bufferedImage).getSubimage(0,0,((BufferedImage)bufferedImage).getWidth(),((BufferedImage)bufferedImage).getHeight());
            drowScreen(bufferedImage1);
            imageCompressor.setResultImage(bufferedImage1);
        });

        directingMachine.setHandler(byte[].class, (byteArrayDifferencies) -> {
            int length=((byte[]) byteArrayDifferencies).length;
            byte[] data=new byte[length];
            System.arraycopy((byte[]) byteArrayDifferencies,0,data,0,length);
            drowDifferencies(data);

        });

        directingMachine.setHandler(NetObject.class, (netObject) -> {
            Main.SEMAPHORE.release();
//            Main.frameCount--;
//            if (Main.frameCount<=0){
//                Main.frameCount=10;
                System.gc();
//            }


            NetObject netObject1 = (NetObject) netObject;
            DataKind dataKind = netObject1.dataKind;
            switch (dataKind) {
                case KEY:
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

    private double getZoomLevel(Rectangle dest,Rectangle src){
        double zoomLevelHeight=dest.getHeight()/src.getHeight();
        double zoomLevelWidth=dest.getWidth()/src.getWidth();
        return Math.min(zoomLevelHeight,Math.min(zoomLevelWidth,1));
    }
    private void drowScreen(BufferedImage bufferedImage) {

        JComponent imageComponent=getImageComponent();

        Graphics g=getImageGraphics();
        //double zoomLevel=getZoomLevel(imageComponent.getBounds(),bufferedImage.getData().getBounds());
        g.drawImage(bufferedImage, 0, 0,imageComponent.getWidth(),imageComponent.getHeight(), imageObserver);
        g.dispose();
    }

    private void drowDifferencies(byte[] differencies) {

        BufferedImage bufferedImage = imageCompressor.decompress(differencies);
        drowScreen(bufferedImage);
    }

    private void createUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            logError(e);
        }
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new JPanel(new BorderLayout());
        frame.add(new JScrollPane(panel));

        imageComponent = getImageComponent();
        imageObserver = imageComponent;
        panel.add(imageComponent, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void show() {
        frame.setBounds(1400, 0, 1000, 500);
        frame.setVisible(true);
    }
}
