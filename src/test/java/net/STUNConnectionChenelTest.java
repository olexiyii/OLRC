package net;

import ol.rc.utils.JSTUNClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class STUNConnectionChenelTest {
    JSTUNClient.ConnectionData connectionData;
    Properties propertiesToLoad;
    ServerSocket serverSocket;
    ServerSocketChannel serverSocketChannel;
    public static final Logger logger = LoggerFactory.getLogger(STUNConnectionChenelTest.class);

    @Before
    public void init(){
        InputStream is = getClass().getClassLoader().getResourceAsStream("stunServers.txt");
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        connectionData=JSTUNClient.getExtermalConnectionData(br.lines()
                .filter(str->!(str.startsWith("#")
                    ||str.startsWith(" ")
                    ||str.length()<10))
                .map(str->str.split(":"))
                .map(strArr->strArr.length==2?strArr:new String[]{strArr[0],"3478"})
                .map(strArr->new InetSocketAddress(strArr[0],Integer.parseInt(strArr[1])))
                .collect(Collectors.toList())
        );

        try {
            serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(connectionData.datagramSocket.getLocalPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Test
    public void testChanelOnJSTUNConnection(){
        Socket socket=null;
        DatagramSocket dsOut=null;
        try {
            dsOut=new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        SocketChannel socketChannelWrite = null;
        try {
            socketChannelWrite = SocketChannel.open();
            socketChannelWrite.connect(new InetSocketAddress("127.0.0.1", connectionData.datagramSocket.getLocalPort()));
        } catch (IOException e) {
            logger.info(e.getStackTrace().toString());
        }

        String msg="test msg";
        String msgResult= "";

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        Future<String> future=executorService.submit(()->{
            int n=100;
            logger.info("Start");
            ByteBuffer buf = ByteBuffer.allocate(1024);

            SocketChannel finalSocketChannel =serverSocketChannel.accept();

            finalSocketChannel.configureBlocking(true);
            int bytesRead = finalSocketChannel.read(buf);


            return new String(Arrays.copyOfRange(buf.array(),0,bytesRead));
        });

        try {

            logger.info(String.valueOf(connectionData.datagramSocket.getLocalPort()));
            logger.info("sleep");
            logger.info(String.valueOf(System.currentTimeMillis()));
            Thread.sleep(2000);
            logger.info(String.valueOf(System.currentTimeMillis()));
            logger.info("wake up");
            socketChannelWrite.write(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException | InterruptedException e) {
            logger.info(e.getStackTrace().toString());
        }

        try {
            msgResult=future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info(e.getStackTrace().toString());
        } catch (ExecutionException e) {
            logger.info(e.getStackTrace().toString());
        } catch (TimeoutException e) {
            logger.info(e.getStackTrace().toString());
        }

        Assert.assertTrue(msg.equals(msgResult));
    }
}
