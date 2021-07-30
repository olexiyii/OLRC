package net;

import junit.framework.TestCase;
import ol.rc.BaseOLRC;
import ol.rc.client.ClientImpl;
import ol.rc.net.IHandler;
import ol.rc.net.NetObject;
import ol.rc.server.DirectingMachineImpl;
import ol.rc.server.ServerImpl;
import ol.rc.utils.JSTUNClient;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.Stack;
import java.util.stream.Collectors;

public class ClientServerTest extends BaseOLRC {
    ClientImpl client;
    ServerImpl server;
    public static Stack<Object> stackHandlerData;
    JSTUNClient.ConnectionData connectionData;
    public ClientServerTest(){
        super(ClientServerTest.class);
    }

    @Before
    public void init() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("stunServers.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        connectionData = JSTUNClient.getExtermalConnectionData(br.lines()
                .filter(str -> !(str.startsWith("#")
                        || str.startsWith(" ")
                        || str.length() < 10))
                .map(str -> str.split(":"))
                .map(strArr -> strArr.length == 2 ? strArr : new String[]{strArr[0], "3478"})
                .map(strArr -> new InetSocketAddress(strArr[0], Integer.parseInt(strArr[1])))
                .collect(Collectors.toList())
        );

        int port = connectionData.datagramSocket.getLocalPort();
        InetSocketAddress address = null;
        try {
            address = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        try {
            server = new ServerImpl(address, new DirectingMachineImpl());
            client = new ClientImpl(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stackHandlerData = new Stack<>();
        server.getDirectingMachine().setHandler(String.class, (obj) -> {
            System.out.println("String handler process:" + obj);
            ClientServerTest.stackHandlerData.push(obj);

        });
        server.getDirectingMachine().setHandler(Integer.class, (obj) -> {
            System.out.println("Integer handler process:" + obj);
            ClientServerTest.stackHandlerData.push(obj);
        });
        server.getDirectingMachine().setHandler(NetObject.class, (obj) -> {
            System.out.println("NetObject handler process:" + obj);
            server.getDirectingMachine().direct(((NetObject)obj).data);

        });
    }

    @Test
    public void directingMachineTest() {
        String dataString = "qwe";

        server.getDirectingMachine().direct(dataString);
        TestCase.assertEquals(stackHandlerData.pop(), dataString);

        Integer dataInteger = 1;
        server.getDirectingMachine().direct(dataInteger);
        TestCase.assertEquals(stackHandlerData.pop(), dataInteger);
    }
    @Test
    public void sendReciveTest()  {
        String dataString = "qwe";
        Integer dataInteger = 1;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("send "+ dataInteger);
        try {
            client.send(NetObject.createOBJECT(dataInteger));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("send "+ dataString);
        try {
            client.send(NetObject.createOBJECT(dataString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("send "+ dataInteger);
        try {
            client.send(NetObject.createOBJECT(dataInteger));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("send Thread.sleep(1000)");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TestCase.assertEquals(stackHandlerData.pop(), dataInteger);
        TestCase.assertEquals(stackHandlerData.pop(), dataString);
        TestCase.assertEquals(stackHandlerData.pop(), dataInteger);
        //TestCase.assertEquals(0, 0);
    }

}
