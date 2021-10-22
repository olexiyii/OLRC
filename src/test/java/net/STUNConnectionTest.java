package net;

import de.javawi.jstun.util.UtilityException;
import ol.rc.utils.JSTUNClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.*;

public class STUNConnectionTest {
    JSTUNClient.ConnectionData connectionData;
    Properties propertiesToLoad;
    ServerSocket serverSocket;
    @Before
    public void init(){
        try {
            propertiesToLoad=new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("stunServers.txt");
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            String strSTUNServer=null;
            while ((strSTUNServer=br.readLine())!=null){
                if (strSTUNServer.startsWith("#")
                        ||strSTUNServer.startsWith(" ")
                        ||strSTUNServer.length()<10){
                    continue;
                }
                System.out.println(strSTUNServer);
                String[] strArr=strSTUNServer.split(":");
                try {
                    if (strArr.length==2){
                        connectionData=JSTUNClient.getExtermalConnectionData(strArr[0],Integer.parseInt(strArr[1]));
                    }

                } catch (UtilityException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if (connectionData!=null){
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            connectionData.datagramSocket.setReuseAddress(true);
            serverSocket=new ServerSocket(connectionData.datagramSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //@Test
    public void testSocketObjectIO(){
        Socket socket=null;
        DatagramSocket dsOut=null;
        try {
            dsOut=new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            socket=new Socket(InetAddress.getByName("127.0.0.1"),connectionData.datagramSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String msg="test msg";
        String msgResult= "";

        ObjectOutputStream outputStream=null;
        try {
            outputStream=new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecutorService executorService= Executors.newSingleThreadExecutor();



        Future<String> future=executorService.submit(()->{
            int n=100;

            Socket socketResult= null;
            try {
                System.out.println("Start: "+System.currentTimeMillis());
                socketResult = serverSocket.accept();
                System.out.println("Ge: "+System.currentTimeMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ObjectInputStream is= null;
            try {
                is = new ObjectInputStream(socketResult.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return (String) is.readObject();
        });

        try {

            System.out.println(connectionData.datagramSocket.getLocalPort());
            System.out.println("sleep");
            System.out.println(System.currentTimeMillis());
            //Thread.sleep(2000);
            System.out.println(System.currentTimeMillis());
            System.out.println("wake up");
            outputStream.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            msgResult=future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(msg.equals(msgResult));
    }
}
