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

package ol.rc.net;

import ol.rc.BaseOLRC;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author Oleksii Ivanov
 */
public class ClientImpl extends BaseOLRC implements IClient {
    private int packegeCount=0;
    private final long startTime;
    private ObjectOutputStream outputStream;
    private InetSocketAddress externalSocket;
    private SocketChannel socketChannelWrite;

    public ClientImpl() {
        super(ClientImpl.class);
        startTime=System.currentTimeMillis();
    }

    public ClientImpl(InetSocketAddress localSocket) throws IOException {
        this();
        setExternalSocket(localSocket);
    }

    @Override
    public void send(NetObject obj) throws IOException {
        getOutputStream().writeObject(obj);

        packegeCount++;
        getOutputStream().flush();
        getOutputStream().reset();
        if ((packegeCount&0x0F)==0){
            BaseOLRC.logInfo("packege per second:"+packegeCount/((System.currentTimeMillis()-startTime)*.0001));
            BaseOLRC.logInfo("packeges: "+packegeCount);
            BaseOLRC.logInfo("second: " + (System.currentTimeMillis()-startTime)*.0001);
        }
    }

    private ObjectOutputStream getOutputStream() {
        if (outputStream == null) {
            long startTime = System.currentTimeMillis();
            do {
                try {
                    socketChannelWrite = SocketChannel.open();
                    socketChannelWrite.socket().connect(externalSocket, 60000);
                    outputStream = new ObjectOutputStream(socketChannelWrite.socket().getOutputStream());
                } catch (IOException e) {
                    error(e);
                }
            } while (outputStream == null
                    && System.currentTimeMillis() - startTime < 120000);
        }
        return outputStream;
    }

    @Override
    public void setExternalSocket(InetSocketAddress externalSocket) {
        this.externalSocket = externalSocket;
        if (outputStream == null) {
            return;
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            logError(e);
        }
        outputStream = null;
    }

    @Override
    public String toString() {
        return "ClientImpl{" +
                "outputStream=" + outputStream +
                ", externalSocket=" + externalSocket +
                ", socketChannelWrite=" + socketChannelWrite +
                '}';
    }

}
