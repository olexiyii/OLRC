package ol.rc.ui;

import com.aparapi.device.Device;
import ol.rc.BaseOLRC;
import ol.rc.Image.IImageCompressor;
import ol.rc.net.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RemoteScreenView extends BaseOLRC implements IRemoteScreenView {
    private IServer server;
    private IClient client;
    private IDirectingMachine directingMachine;
    private IImageCompressor imageCompressor;
    private JFrame frame;
    private JPanel panel;
    private JComponent imageComponent;
    private ImageObserver imageObserver;
    private OlMouseInputAdapter mouseInputAdapter;
    private OlKeyAdapter keyAdapter;
    //private int count=1;

    public RemoteScreenView() {
        super(RemoteScreenView.class);
        createUI();
    }

    public RemoteScreenView(IServer server, IImageCompressor imageCompressor, IClient client, Device device) {
        this(server, imageCompressor, client);
        imageCompressor.setDeviceForParallelCalc(device);
    }

    public RemoteScreenView(IServer server, IImageCompressor imageCompressor, IClient client) {
        this();
        this.server = server;
        this.client = client;
        directingMachine = server.getDirectingMachine();
        this.imageCompressor = imageCompressor;
        setClient(client);
        createDirectingMachine();
    }

    public void setClient(IClient client) {
        this.client = client;
        keyAdapter.setClient(client);
        mouseInputAdapter.setClient(client);
    }

    private Graphics getImageGraphics() {
        return getImageComponent().getGraphics();
    }

    private JComponent createImageComponent() {
        JPanel result = new JPanel();
        mouseInputAdapter = new OlMouseInputAdapter(client);
        result.addMouseListener(mouseInputAdapter);
        keyAdapter = new OlKeyAdapter(client);
        result.addKeyListener(keyAdapter);
        result.setFocusable(true);
        return result;
    }

    private JComponent getImageComponent() {
        if (imageComponent == null) {
            imageComponent = createImageComponent();
        }
        return imageComponent;
    }

    private void createDirectingMachine() {
        //BaseOLRC.logInfo("createDirectingMachine " + getClass().getName());

        directingMachine.setHandler(BufferedImage.class, (bufferedImage) -> {
            BufferedImage bufferedImage1 = ((BufferedImage) bufferedImage).getSubimage(0, 0, ((BufferedImage) bufferedImage).getWidth(), ((BufferedImage) bufferedImage).getHeight());
            drowScreen(bufferedImage1);
            imageCompressor.setResultImage(bufferedImage1);
        });

        directingMachine.setHandler(byte[].class, (byteArrayDifferencies) -> {
            int length = ((byte[]) byteArrayDifferencies).length;
            byte[] data = new byte[length];
            System.arraycopy((byte[]) byteArrayDifferencies, 0, data, 0, length);
            drowDifferencies(data);

        });

        directingMachine.setHandler(NetObject.class,null);
        directingMachine.setHandler(InetSocketAddress.class,null);

        directingMachine.setHandler(NetObject.class, (netObject) -> {
            System.gc();

            NetObject netObject1 = (NetObject) netObject;
            DataKind dataKind = netObject1.dataKind;
            switch (dataKind) {
                case KEY:
                    break;
                case SCREEN_INITIAL:
                    directingMachine.direct(netObject1.data);
                    netObject1 = null;
                    break;
                case SCREEN_DIFFERENCES:
                    directingMachine.direct(netObject1.data);
                    netObject1 = null;
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

    private double getZoomLevel(Rectangle dest, Rectangle src) {
        double zoomLevelHeight = dest.getHeight() / src.getHeight();
        double zoomLevelWidth = dest.getWidth() / src.getWidth();
        return Math.min(zoomLevelHeight, Math.min(zoomLevelWidth, 1));
    }

    private void drowScreen(BufferedImage bufferedImage) {

        JComponent imageComponent = getImageComponent();

        Graphics g = getImageGraphics();
        //double zoomLevel=getZoomLevel(imageComponent.getBounds(),bufferedImage.getData().getBounds());
        g.drawImage(bufferedImage, 0, 0, imageComponent.getWidth(), imageComponent.getHeight(), imageObserver);
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
        frame.setLocation(0,0);
        frame.setSize(1000,500);
        frame.setVisible(true);
    }

    @Override
    public void setImageCompressor(IImageCompressor imageCompressor) {
        IImageCompressor imageCompressorOld = this.imageCompressor;
        this.imageCompressor = imageCompressor;
        this.imageCompressor.setResultArray(imageCompressorOld.getResultData());
    }

    @Override
    public void start() {
//        setClient(client);
        server.start();
        show();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            client.send(NetObject.createSCREEN_INITIAL(null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        server.stop();
        frame.setVisible(false);
        frame = null;
    }
}
