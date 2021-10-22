/*
 * Copyright (c) 2021. Oleksii Ivanov
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package ol.rc.utils;

import de.javawi.jstun.attribute.*;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;
import ol.rc.BaseOLRC;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * This is utility class to get connection data (external(address,port) and local(address,port))
 * @author Oleksii Ivanov
 */
public final class JSTUNClient extends BaseOLRC {
    public static class ConnectionData{
        public final InetAddress address;
        public final int port;
        public final DatagramSocket datagramSocket;
        public ConnectionData(DatagramSocket datagramSocket,InetAddress address, int port){
            this.datagramSocket=datagramSocket;
            this.address=address;
            this.port=port;
        }
    }
    private JSTUNClient(){}

    public static ConnectionData getExtermalConnectionData(String stunServerAddress, int stunServerPort) throws UtilityException, IOException {
        return getExtermalConnectionData(InetAddress.getByName(stunServerAddress),stunServerPort);
    }

    public static ConnectionData getExtermalConnectionData(InetAddress stunServer, int stunServerPort) throws UtilityException, IOException {
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

            logInfo("STUN: Binding Request sent.");

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
            logInfo("ChangedAddress ca:"+ca);
            if (ec != null) {
                logInfo(""+ec.getResponseCode()+","+ec.getReason());
                logInfo("Message header contains an Errorcode message attribute.");
                return null;
            }
            return new ConnectionData(datagramSocket,ma.getAddress().getInetAddress(),ma.getPort());
        } catch (MessageHeaderParsingException | MessageAttributeParsingException | ClassNotFoundException e) {
            logError(e);
        }
        return null;
    }

    public static ConnectionData getExtermalConnectionData(List<InetSocketAddress> stunServers){
        ConnectionData connectionData=null;
        for (InetSocketAddress stunServer:stunServers){
            try {
                connectionData=getExtermalConnectionData(stunServer.getAddress(),stunServer.getPort());
            } catch (UtilityException e) {
                logError(e);
            }catch (IOException e) {
                logError(e);
            }catch (Exception e) {
                logError(e);
            }
            if (connectionData!=null){
                break;
            }
        }
        if (connectionData==null){
            logInfo("no stun Servers :(");
        }
        return connectionData;
    }



}
