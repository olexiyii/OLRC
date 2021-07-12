package ol.rc.utils;

import de.javawi.jstun.attribute.*;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public final class JSTUNClient {
    public static class ConnectionData{
        public InetAddress address;
        public int port;
        public DatagramSocket datagramSocket;
        public ConnectionData(DatagramSocket datagramSocket,InetAddress address, int port){
            this.datagramSocket=datagramSocket;
            this.address=address;
            this.port=port;
        }
    }
    private JSTUNClient(){}

    public static ConnectionData getExtermalConnectionData1(InetAddress stunServerAddress, int stunServerPort) throws UtilityException, IOException {
        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        // sendMH.generateTransactionID();

        // add an empty ChangeRequest attribute. Not required by the
        // standard,
        // but JSTUN server requires it

        ChangeRequest changeRequest = new ChangeRequest();
        sendMH.addMessageAttribute(changeRequest);

        byte[] data = sendMH.getBytes();


        DatagramSocket datagramSocket = null;
        //==================================
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements() && datagramSocket == null) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
            while (iaddresses.hasMoreElements()  && datagramSocket == null) {
                InetAddress iaddress = iaddresses.nextElement();
                try {
                    if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
                        if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
                            try {
                                datagramSocket = new DatagramSocket(new InetSocketAddress(iaddress,0));
                                datagramSocket.setReuseAddress(true);
                                datagramSocket.connect(stunServerAddress, stunServerPort);
                                int timeout = 300;
                                datagramSocket.setSoTimeout(timeout);
                                //System.out.println(datagramSocket.isConnected());

                                sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                                sendMH.generateTransactionID();

                                changeRequest = new ChangeRequest();
                                sendMH.addMessageAttribute(changeRequest);

                                data = sendMH.getBytes();
                                DatagramPacket send = new DatagramPacket(data, data.length);
                                datagramSocket.send(send);
                                break;
                            }catch (Exception e){
                                datagramSocket = null;
                                continue;
                            }

                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        //==================================
        datagramSocket.setReuseAddress(true);

        DatagramPacket p = new DatagramPacket(data, data.length, stunServerAddress, stunServerPort);

        datagramSocket.send(p);

        DatagramPacket rp;

        rp = new DatagramPacket(new byte[32], 32);

        datagramSocket.receive(rp);
        MessageHeader receiveMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingResponse);

        try {
            receiveMH.parseAttributes(rp.getData());
        } catch (MessageAttributeParsingException e) {
            e.printStackTrace();
        }
        MappedAddress ma = (MappedAddress) receiveMH
                .getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);

        return new ConnectionData(datagramSocket,ma.getAddress().getInetAddress(),ma.getPort());
    }
    public static ConnectionData getExtermalConnectionData(String stunServerAddress, int stunServerPort) throws UtilityException, IOException {
        return getExtermalConnectionData(InetAddress.getByName(stunServerAddress),stunServerPort);
    }
    public static ConnectionData getExtermalConnectionData11(InetAddress stunServer, int stunServerPort) throws UtilityException, IOException {
//        DatagramSocket socketTest1 = new DatagramSocket(new InetSocketAddress(sourceIaddress, sourcePort));
        try {

            DatagramSocket datagramSocket = null;
            MessageHeader sendMH = null;
            ChangeRequest changeRequest = null;

            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements() && datagramSocket == null) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
                while (iaddresses.hasMoreElements()  && datagramSocket == null) {
                    InetAddress iaddress = iaddresses.nextElement();
                    if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
                        if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
                            try {
                                datagramSocket = new DatagramSocket(new InetSocketAddress(iaddress,0));
                                datagramSocket.setReuseAddress(true);
                                datagramSocket.connect(stunServer, stunServerPort);
                                int timeout = 300;
                                datagramSocket.setSoTimeout(timeout);
                                System.out.println(datagramSocket.isConnected());

                                sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                                sendMH.generateTransactionID();

                                changeRequest = new ChangeRequest();
                                sendMH.addMessageAttribute(changeRequest);

                                byte[] data = sendMH.getBytes();
                                DatagramPacket send = new DatagramPacket(data, data.length);
                                datagramSocket.send(send);
                                break;
                            }catch (Exception e){
                                datagramSocket = null;
                                continue;
                            }

                        }
                    }
                }
            }

            //LOGGER.debug("Test 1: Binding Request sent.");

            MessageHeader receiveMH = new MessageHeader();
            while (!(receiveMH.equalTransactionID(sendMH))) {
                DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                datagramSocket.receive(receive);
                receiveMH = MessageHeader.parseHeader(receive.getData());
                receiveMH.parseAttributes(receive.getData());
            }

            MappedAddress ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
            ChangedAddress ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
            ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
            if (ec != null) {
//            di.setError(ec.getResponseCode(), ec.getReason());
//            LOGGER.debug("Message header contains an Errorcode message attribute.");
                return null;
            }
            //return ma;
            return new ConnectionData(datagramSocket,ma.getAddress().getInetAddress(),ma.getPort());
        } catch (MessageHeaderParsingException | MessageAttributeParsingException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
//        MappedAddress ma = (MappedAddress) receiveMH
//                .getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
//        //System.out.println(ma.getAddress() + " " + ma.getPort());
//        //return new Socket(ma.getAddress().getInetAddress(),ma.getPort());
//        return ma;
    }

    public static ConnectionData getExtermalConnectionData(InetAddress stunServer, int stunServerPort) throws UtilityException, IOException {
//        DatagramSocket socketTest1 = new DatagramSocket(new InetSocketAddress(sourceIaddress, sourcePort));
        try {

            DatagramSocket datagramSocket = null;
            MessageHeader sendMH = null;
            ChangeRequest changeRequest = null;

            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements() && datagramSocket == null) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
                while (iaddresses.hasMoreElements()  && datagramSocket == null) {
                    InetAddress iaddress = iaddresses.nextElement();
                    if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
                        if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
                            try {
                                datagramSocket = new DatagramSocket(new InetSocketAddress(iaddress,0));
                                datagramSocket.setReuseAddress(true);
                                datagramSocket.connect(stunServer, stunServerPort);
                                int timeout = 300;
                                datagramSocket.setSoTimeout(timeout);
                                //System.out.println(datagramSocket.isConnected());

                                sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                                sendMH.generateTransactionID();

                                changeRequest = new ChangeRequest();
                                sendMH.addMessageAttribute(changeRequest);

                                byte[] data = sendMH.getBytes();
                                DatagramPacket send = new DatagramPacket(data, data.length);
                                datagramSocket.send(send);
                                break;
                            }catch (Exception e){
                                datagramSocket = null;
                                continue;
                            }

                        }
                    }
                }
            }

            //LOGGER.debug("Test 1: Binding Request sent.");

            MessageHeader receiveMH = new MessageHeader();
            while (!(receiveMH.equalTransactionID(sendMH))) {
                DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                datagramSocket.receive(receive);
                receiveMH = MessageHeader.parseHeader(receive.getData());
                receiveMH.parseAttributes(receive.getData());
            }

            MappedAddress ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
            ChangedAddress ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
            ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
            if (ec != null) {
//            di.setError(ec.getResponseCode(), ec.getReason());
//            LOGGER.debug("Message header contains an Errorcode message attribute.");
                return null;
            }
            return new ConnectionData(datagramSocket,ma.getAddress().getInetAddress(),ma.getPort());
        } catch (MessageHeaderParsingException | MessageAttributeParsingException | ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return null;
//        MappedAddress ma = (MappedAddress) receiveMH
//                .getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
//        //System.out.println(ma.getAddress() + " " + ma.getPort());
//        //return new Socket(ma.getAddress().getInetAddress(),ma.getPort());
//        return ma;
    }

    public static ConnectionData getExtermalConnectionData(List<InetSocketAddress> stunServers){
        ConnectionData connectionData=null;
        for (InetSocketAddress stunServer:stunServers){
            try {
                connectionData=getExtermalConnectionData(stunServer.getAddress(),stunServer.getPort());
            } catch (UtilityException e) {
                //e.printStackTrace();
            }catch (IOException e) {
                //e.printStackTrace();
            }catch (Exception e) {
                //e.printStackTrace();
            }
            if (connectionData!=null){
                break;
            }
        }
        if (connectionData==null){
            System.out.println("no stun Servers :(");
        }
        return connectionData;
    }



}
