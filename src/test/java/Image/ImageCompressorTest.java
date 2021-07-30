package Image;

import ol.rc.Image.IImageCompressor;
import ol.rc.Image.ImageCompressorByte;
import ol.rc.net.NetObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ImageCompressorTest {
    public static int indexImg=0;
    List<BufferedImage> listScreenShots;
    List<BufferedImage> listScreenShotsRestored;
    public IImageCompressor imageCompressor;
    public IImageCompressor imageDecompressor;

    @Before
    public void init() {
        imageCompressor =new ImageCompressorByte();
        imageDecompressor=new ImageCompressorByte();
        listScreenShots=new ArrayList<>();
    }

    @Test
    public void packTest() throws IOException, ClassNotFoundException {
        InputStream inputStream=Files.newInputStream(Paths.get("objects.txt"));
        ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
        int imgCount=3;
        for (int i=0;i<imgCount;i++){
            NetObject netObject= (NetObject) objectInputStream.readObject();
            if (i>0){
                byte[] netPacked=Files.readAllBytes(Paths.get(getFilenameBytesDiffCompressed(i)));
                try {
                    Files.write(Paths.get(getFilenameBytesDiffCompressed(i)+"fromNet"),(byte[]) netObject.data, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Assert.assertArrayEquals((byte[]) netObject.data,netPacked);
            }
        }
    }
    String getFilenameBytes(int index){
        return "bytes"+index;
    }
    String getFilenameBytesDiffCompressed(int index){
        return "bytesDiffCompressed"+index;
    }
    private void saveImageData(BufferedImage screenImage,int imgIndex) {
        NetObject objToSend;
        System.out.println("ScreenPublisherImpl getObjectToSend");
        byte[] bytesResult;
        if (imgIndex==0) {
            imageCompressor.setResultImage(screenImage);
        } else {
            System.out.println("ScreenPublisherImpl getObjectToSend SCREEN_DIFFERENCES");
            byte[] DIFFERENCEScompressed= imageCompressor.compress(screenImage);
            try {
                Files.write(Paths.get(getFilenameBytesDiffCompressed(imgIndex)),DIFFERENCEScompressed, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bytesResult= imageCompressor.getResultData();
        try {
            Files.write(Paths.get(getFilenameBytes(imgIndex)),bytesResult, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
