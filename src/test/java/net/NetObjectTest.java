package net;

import ol.rc.net.DataKind;
import ol.rc.net.NetObject;
import ol.rc.screen.ScreenReaderImpl;
import ol.rc.server.DirectingMachineImpl;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class NetObjectTest {
    @Before
    public void init() {
    }

    @Test
    public void SCREEN_DIFFERENCESTest() throws IOException, ClassNotFoundException {

        int imgCount=3;
        String filename="NetObject.txt";

        OutputStream outputStream= Files.newOutputStream(Paths.get(filename));
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);


        byte[] data=new byte[]{1,2,3};

        for (int i=0;i<imgCount;i++){
            NetObject netObject=new NetObject(DataKind.SCREEN_DIFFERENCES,data);
            objectOutputStream.writeObject(netObject);
            objectOutputStream.flush();
        }
        objectOutputStream.close();
        outputStream.flush();
        outputStream.close();

        InputStream inputStream=Files.newInputStream(Paths.get(filename));
        ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);

        for (int i=0;i<imgCount;i++){
            NetObject netObject= (NetObject) objectInputStream.readObject();

        }

    }
}
