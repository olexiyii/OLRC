package ol.rc.ui;

import ol.rc.BaseOLRC;
import ol.rc.Image.IImageCompressor;
import ol.rc.Main;
import ol.rc.net.DataKind;
import ol.rc.net.IDirectingMachine;
import ol.rc.net.IServer;
import ol.rc.net.NetObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class RemoteScreenView extends BaseOLRC {
    Graphics imageGraphics;
    private IServer server;
    private IDirectingMachine directingMachine;
    private IImageCompressor imageCompressor;
    private JFrame frame;
    private JPanel panel;
    private JPanel imagePanel;
    private ImageObserver imageObserver;


    public RemoteScreenView(IServer server, IImageCompressor imageCompressor) {
        super(RemoteScreenView.class);
        createUI();
        this.server = server;

        directingMachine = server.getDirectingMachine();
        this.imageCompressor = imageCompressor;
        createDirectingMachine();


        //createDirectingMachine();

    }

    Graphics getImageGraphics() {
        if (imageGraphics == null) {
            imageGraphics = imagePanel.getGraphics();
        }
        return imageGraphics;
    }

    private void createDirectingMachine() {


        directingMachine.setHandler(BufferedImage.class, (bufferedImage) -> {
            drowScreen((BufferedImage) bufferedImage);
            imageCompressor.setResultImage((BufferedImage) bufferedImage);
        });

        directingMachine.setHandler(byte[].class, (byteArrayDifferencies) -> {
            drowDifferencies((byte[]) byteArrayDifferencies);
        });

        directingMachine.setHandler(NetObject.class, (netObject) -> {
            Main.SEMAPHORE.release();

            NetObject netObject1 = (NetObject) netObject;
            DataKind dataKind = netObject1.dataKind;
            switch (dataKind) {
                case KEY:
                    break;
                case SCREEN_INITIAL:
                    directingMachine.direct(netObject1.data);
                    break;
                case SCREEN_DIFFERENCES:
                    directingMachine.direct(netObject1.data);
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

    private void drowScreen(BufferedImage bufferedImage) {
        getImageGraphics().drawImage(bufferedImage, 0, 0, imageObserver);
    }

    private void drowDifferencies(byte[] differencies) {

        BufferedImage bufferedImage = imageCompressor.decompress(differencies);
        getImageGraphics().drawImage(bufferedImage, 0, 0, imageObserver);
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

        imagePanel = new JPanel();
        imageObserver = imagePanel;
        panel.add(imagePanel, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void show() {
        frame.setBounds(1400, 0, 1000, 500);
        frame.setVisible(true);
    }
}
