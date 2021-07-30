package client;

import ol.rc.BaseOLRC;
import ol.rc.Image.IImageCompressor;
import ol.rc.Image.ImageCompressorByte;
import ol.rc.net.DataKind;
import ol.rc.net.IDirectingMachine;
import ol.rc.net.NetObject;
import ol.rc.screen.IScreenReader;
import ol.rc.screen.ScreenReaderImpl;
import ol.rc.server.DirectingMachineImpl;
import ol.rc.utils.Packer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

public class ScreenReaderTest extends BaseOLRC {
    public IImageCompressor imageСompressor;
    public IImageCompressor imageDecompressor;
    IScreenReader screenReader;
    IDirectingMachine directingMachine;
    Packer packer;
    List<BufferedImage> listScreenShots;
    List<NetObject> listSendedObj;
    List<NetObject> listRecivedObj;
    List<BufferedImage> listScreenShotsRecived;

    BufferedImage screenImageReaded;

    public ScreenReaderTest() {
        super(ScreenReaderTest.class);
    }

    @Before
    public void init() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();
        GraphicsDevice gd = screenDevices[0];
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        screenReader = new ScreenReaderImpl(gd, bounds);

        imageСompressor = new ImageCompressorByte();
        imageDecompressor = new ImageCompressorByte();
        packer = new Packer();


        listScreenShots = new ArrayList<>();
        listScreenShotsRecived = new ArrayList<>();
        listSendedObj = new ArrayList<>();
        listRecivedObj = new ArrayList<>();
        directingMachine = new DirectingMachineImpl();
        createDirectingMachine();
    }

    @Test
    public void SCREEN_DIFFERENCESTest() throws IOException, ClassNotFoundException {

        int imgCount = 3;
        String filename = "objects.txt";

//        OutputStream outputStream=Files.newOutputStream(Paths.get(filename));
//        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
//
//        for (int i=0;i<imgCount;i++){
//            BufferedImage screenImage = screenReader.getImage();
//            listScreenShots.add(screenImage);
//            NetObject netObject=getObjectToSend(screenImage);
//            objectOutputStream.writeObject(netObject);
//            listSendedObj.add(netObject);
//            objectOutputStream.flush();
//            BufferedImage bufferedImage=listScreenShots.get(i);
//            ImageIO.write(bufferedImage,"png",Paths.get("out"+i+".png").toFile());
//        }
//
//        objectOutputStream.close();
//        outputStream.flush();
//        outputStream.close();

        InputStream inputStream = Files.newInputStream(Paths.get(filename));
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        for (int i = 0; i < imgCount; i++) {
            NetObject netObject = (NetObject) objectInputStream.readObject();
            listRecivedObj.add(netObject);
            directingMachine.direct(netObject);
            listScreenShotsRecived.add(screenImageReaded);
        }

        for (int i = 0; i < imgCount; i++) {
            BufferedImage bufferedImageRecived = listScreenShotsRecived.get(i);
            ImageIO.write(bufferedImageRecived, "png", Paths.get("in" + i + ".png").toFile());

        }

    }

    BufferedImage getScreenImage(int i) throws IOException {
        return ImageIO.read(Paths.get("out" + i + ".png").toFile());
    }

    private void writeImageToNetObject(int i) throws IOException {
        String filename = "objects" + i + ".txt";
        OutputStream outputStream = Files.newOutputStream(Paths.get(filename));
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        NetObject netObject = getObjectToSend(getScreenImage(i));
        objectOutputStream.writeObject(netObject);

        objectOutputStream.close();
        outputStream.flush();
        outputStream.close();

    }

    private void writeBytes(byte[] data, String filename) {
        try {
            Files.write(Paths.get(filename), data, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private byte[] readBytes( String filename) {
        byte[] data=null;
        try {
            data=Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
    @Test
    public void createTestImages() throws IOException {
        int width=8, height=8;
        int bytesPerPixel=3;
        int i=0;
        byte[] tmp=new byte[width* height*bytesPerPixel];
//        tmp[bytesPerPixel*(3*width+3)+0]=0x01;
//        tmp[bytesPerPixel*(3*width+3)+1]=0x0F;
//        tmp[bytesPerPixel*(3*width+3)+2]= (byte) 0x8F;
        WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(tmp, tmp.length), width, height, width * bytesPerPixel, bytesPerPixel, new int[]{0, 1, 2}, null);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        bufferedImage.setData(raster);
        bufferedImage.setRGB(3,3,0x010F8F);
        ImageIO.write(bufferedImage,"png",Paths.get("out"+i+".png").toFile());
        i++;

        bufferedImage.setRGB(0,0,0x010F8F);
        ImageIO.write(bufferedImage,"png",Paths.get("out"+i+".png").toFile());
        i++;

        bufferedImage.setRGB(0,0,0);
        bufferedImage.setRGB(1,0,0x010F8F);

        ImageIO.write(bufferedImage,"png",Paths.get("out"+i+".png").toFile());
        i++;
    }

    @Test
    public void writeNetObjectTest() throws IOException, ClassNotFoundException, DataFormatException {

        int imgCount = 3;

        for (int i = 0; i < imgCount; i++) {
            writeImageToNetObject(i);

            byte[] data = imageСompressor.getResultData();
            writeBytes(data, "data_" + i);
            byte[] dataPacked = packer.pack(data);
            writeBytes(dataPacked, "dataPacked_" + i);
        }

        for (int i = 0; i < imgCount; i++) {
            if (i==0){
                BufferedImage bufferedImage =ImageIO.read(Paths.get("out0.png").toFile());
                imageDecompressor.setResultImage(bufferedImage);

//                byte[] tmp=imageDecompressor.getResultData();
//                int width=bufferedImage.getWidth();
//                int height=bufferedImage.getHeight();
//                int bytesPerPixel=3;
//                WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(tmp, tmp.length), width, height, width * bytesPerPixel, bytesPerPixel, new int[]{0, 1, 2}, null);
//                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//                img.setData(raster);
                ImageIO.write(bufferedImage,"png",Paths.get("in"+i+".png").toFile());
                continue;
            }
            byte[] dataPacked =readBytes( "dataPacked_" + i);
            byte[] data = packer.unpack(dataPacked);
            writeBytes(data, "dataUnpacked_" + i);

            BufferedImage bufferedImage = imageDecompressor.decompress(dataPacked);
            ImageIO.write(bufferedImage,"png",Paths.get("in"+i+".png").toFile());
        }
        for (int i=1;i<imgCount;i++){
            byte[] dataSrc=readBytes("data_" + i);
            byte[] dataUnpack=readBytes("dataUnpacked_" + i);
            Assert.assertArrayEquals(dataSrc,dataUnpack);
        }

    }


    private NetObject getObjectToSend(BufferedImage screenImage) {
        NetObject objToSend;
        System.out.println("ScreenPublisherImpl getObjectToSend");
        //objToSend = NetObject.createSCREEN_INITIAL(screenImage);
        if (screenReader.isNewSettings()) {
            objToSend = NetObject.createSCREEN_INITIAL(screenImage);
            screenReader.resetNewSettings();
            imageСompressor.setResultImage(screenImage);
        } else {
            System.out.println("ScreenPublisherImpl getObjectToSend SCREEN_DIFFERENCES");
            objToSend = NetObject.createSCREEN_DIFFERENCES(imageСompressor.compress(screenImage));
        }
        return objToSend;
    }

    private void createDirectingMachine() {

        directingMachine.setHandler(BufferedImage.class, (bufferedImage) -> {
            drowScreen((BufferedImage) bufferedImage);
            //imageDecompressor.setResultImage((BufferedImage) bufferedImage);

//            imageCompressor.setImageSettings(((BufferedImage) bufferedImage).getWidth(),((BufferedImage) bufferedImage).getHeight());
//            BufferedImage img = ImageConverter.convertTo24Bit((BufferedImage) bufferedImage);
//            byte[] tmp = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();

        });

        directingMachine.setHandler(byte[].class, (byteArrayDifferencies) -> {
            drowDifferencies((byte[]) byteArrayDifferencies);
        });

        directingMachine.setHandler(NetObject.class, (netObject) -> {
            DataKind dataKind = ((NetObject) netObject).dataKind;
            switch (dataKind) {
                case SCREEN_INITIAL:
                    directingMachine.direct(((NetObject) netObject).data);
                    break;
                case SCREEN_DIFFERENCES:
                    directingMachine.direct((byte[]) ((NetObject) netObject).data);
                    break;
                default:
                    System.out.println("dataKind:" + dataKind);
                    break;
            }
        });

    }

    private void drowScreen(BufferedImage bufferedImage) {
//        ImageObserver imageObserver=new ImageObserver() {
//            @Override
//            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
//                return false;
//            }
//        }

        screenImageReaded = bufferedImage;
        imageDecompressor.setResultImage(bufferedImage);
    }

    private void drowDifferencies(byte[] differencies) {
        System.out.println("drowDifferencies:" + differencies.length);

        screenImageReaded = imageDecompressor.decompress(differencies);

    }

}
