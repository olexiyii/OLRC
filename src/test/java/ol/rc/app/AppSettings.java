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

package ol.rc.app;

import ol.rc.BaseOLRC;
import ol.rc.net.IClient;
import ol.rc.net.IRemoteScreenView;
import ol.rc.net.IServer;
import ol.rc.publish.IScreenPublisher;
import ol.rc.ui.settingsEditors.IChangesSubscriber;
import ol.rc.ui.settingsEditors.Property;
import ol.rc.utils.JSTUNClient;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

public class AppSettings extends BaseOLRC implements IChangesSubscriber {
    private static AppSettings appSettings;

    private JSTUNClient.ConnectionData connectionData;
    private InetSocketAddress localAddress;
    private InetSocketAddress externalAddress;
    private InetSocketAddress remoteAddress;
    private GraphicsDevice gd;

    private AppType appType;

    private IClient client;
    private IServer server;

    public IScreenPublisher getScreenPublisher() {
        return screenPublisher;
    }

    public void setScreenPublisher(IScreenPublisher screenPublisher) {
        this.screenPublisher = screenPublisher;
    }

    public IRemoteScreenView getRemoteScreenView() {
        return remoteScreenView;
    }

    public void setRemoteScreenView(IRemoteScreenView remoteScreenView) {
        this.remoteScreenView = remoteScreenView;
    }

    private IScreenPublisher screenPublisher;
    private IRemoteScreenView remoteScreenView;
    private int fps;

    public static enum PUBLIC_SETTINGS_KIND{

    }

    public static AppSettings getAppSettings() {
        if (appSettings ==null){
            appSettings =new AppSettings();
        }
        return appSettings;
    }


    public AppSettings(){
        remoteAddress=new InetSocketAddress("127.0.0.1",5555);
        localAddress=new InetSocketAddress("127.0.0.1",5555);
        externalAddress=new InetSocketAddress("127.0.0.1",5555);
        appType=AppType.MANIPULATOR;
    }

    public AppSettings(IClient client, IServer server){
        setClient(client);
        setServer(server);
    }


    public AppType getAppType() {
        return appType;
    }

    public void setAppType(AppType appType) {
        this.appType = appType;
        if (this.appType == AppType.MANIPULATOR){
            try {
                screenPublisher.stop();
                remoteScreenView.start();
            }catch (Exception e){
                logError(e);
            }
        }else if (this.appType == AppType.PUBLISHER){
            try {
                remoteScreenView.stop();
                screenPublisher.start();
            }catch (Exception e){
                logError(e);
            }
        }
    }

    private List<InetSocketAddress> getSTUNServers() {

        InputStream is = getClass().getClassLoader().getResourceAsStream("stunServers.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        int defaultSTUNPort = 3478;

        return br.lines()
                .filter(str -> !(str.startsWith("#") || str.length() < 10))
                .map(str -> {
                    String[] addressPort = str.split(":");
                    int port = addressPort.length == 2 ? Integer.parseInt(addressPort[1]) : defaultSTUNPort;
                    return new InetSocketAddress(addressPort[0], port);
                }).collect(Collectors.toList());
    }

    private JSTUNClient.ConnectionData getConnectionData() {
        if (connectionData == null) {
            connectionData = JSTUNClient.getExtermalConnectionData(getSTUNServers());
        }
        return connectionData;
    }

    public InetSocketAddress getLocalAddress() {
        if (localAddress == null) {

            setLocalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getLocalAddress(), getConnectionData().datagramSocket.getLocalPort()));
            setExternalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getInetAddress(), getConnectionData().datagramSocket.getPort()));
        }
        return localAddress;
    }
    public void resetLocalAddress(){
        localAddress = null;
    }
    public void resetExternalAddress(){
        externalAddress = null;
    }

    private void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
        server.setLocalSocketAddress(this.localAddress);
    }

    public InetSocketAddress getExternalAddress() {
        if (externalAddress == null) {

            setLocalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getLocalAddress(), getConnectionData().datagramSocket.getLocalPort()));
            setExternalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getInetAddress(), getConnectionData().datagramSocket.getPort()));
        }
        return externalAddress;
    }

    private void setExternalAddress(InetSocketAddress externalAddress) {
        this.externalAddress = externalAddress;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        client.setExternalSocket(this.remoteAddress);
    }

    public IClient getClient() {
        return client;
    }
    public void setClient(IClient client) {
        this.client = client;
    }

    public IServer getServer() {
        return server;
    }

    public void setServer(IServer server) {
        this.server = server;
    }

    @Override
    public void receiveChanges(String changesName, Object changesValue) {
        if (changesName.equals(EXPECTED_CHANGES.FPS)){
            setFps((Integer) changesValue);
        }else if (changesName.equals(EXPECTED_CHANGES.APPTYPE)){
            setAppType((AppType) changesValue);
        }else if (changesName.equals(EXPECTED_CHANGES.REMOTEADDRESS)){
            setRemoteAddress((InetSocketAddress) changesValue);
        }else if (changesName.equals(EXPECTED_CHANGES.LOCALADDRESS)){
            setLocalAddress((InetSocketAddress) changesValue);
        }
    }

    @Override
    public Property[] getExpectedChanges() {
        return new Property[]{
                //new Property(EXPECTED_CHANGES.FPS,getFps(),false,this),
//                new Property(EXPECTED_CHANGES.REMOTEADDRESS,getRemoteAddress(),false,this),
//                new Property(EXPECTED_CHANGES.LOCALADDRESS,getLocalAddress(),false,this),
                new Property(EXPECTED_CHANGES.APPTYPE,AppType.class,getAppType(),false,this)
        };
    }

    @Override
    public String getName() {
        return "General settings";
    }

    private final class EXPECTED_CHANGES {

        public static final String FPS = "FPS";
        public static final String REMOTEADDRESS = "Remote address";
        public static final String LOCALADDRESS = "Local address";
        public static final String APPTYPE = "App type";

        private EXPECTED_CHANGES() {
        }
    }
}
