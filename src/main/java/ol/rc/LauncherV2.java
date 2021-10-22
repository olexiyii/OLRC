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

package ol.rc;

import ol.rc.Image.IImageCompressor;
import ol.rc.Image.ImageCompressorByte;
import ol.rc.app.AppType;
import ol.rc.app.PublisherSettings;
import ol.rc.app.PublisherSettingsV2;
import ol.rc.net.*;
import ol.rc.publish.IScreenPublisher;
import ol.rc.publish.ScreenPublisherImpl;
import ol.rc.screen.IScreenReader;
import ol.rc.screen.ScreenReaderImpl;
import ol.rc.server.DirectingMachineImpl;
import ol.rc.server.ServerImpl;
import ol.rc.ui.RemoteScreenView;
import ol.rc.ui.settingsEditors.IChangesSubscriber;
import ol.rc.ui.settingsEditors.ISettingsEditor;
import ol.rc.ui.settingsEditors.ISettingsEditorFabric;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This class provide user interface to edit available settings and launch
 * -Publisher of local PC screen or
 * -Manipulator of remote PC
 *
 * @author Oleksii Ivanov
 */

public class LauncherV2 extends JFrame {
    private final PublisherSettings publisherSettings;
    private boolean remoteAddressReceived=false;
    private ServerImpl server = null;
    private IClient client = null;

    private List<ISettingsEditor> settingsEditorList = null;

    private RemoteScreenView remoteScreenView = null;
    private IScreenPublisher screenPublisher = null;

    public LauncherV2() {
        publisherSettings = new PublisherSettings();
        createUI();
    }

    public void start() {
        BaseOLRC.logInfo(String.format("start: %s, %s, %s, %s, %s",
                server,client,publisherSettings.getAppType(),publisherSettings.getRemoteAddress(),publisherSettings.getExternalAddress() ) );
        if (getServer() != null) {
            getServer().start();
        }

        if (getClient() != null) {
            if(!remoteAddressReceived){
                try {
                    getClient().send(NetObject.createInetSocketAddress(publisherSettings.getExternalAddress()));
                } catch (IOException e) {
                    BaseOLRC.logError(e);
                }
            }

        }
        if(getServer() != null && getClient() != null){
            launch();
        }
    }

    private IClient getClient(){
        if (publisherSettings.getRemoteAddress() != null && client ==null){
            try {
                client = new ClientImpl(publisherSettings.getRemoteAddress());
            } catch (IOException e) {
                BaseOLRC.logError(e);
                return null;
            }
        }
        return client;
    }

    public void stop() {

        if (remoteScreenView != null) {
            remoteScreenView.stop();
            remoteScreenView = null;
        }
        if (screenPublisher != null) {
            screenPublisher.stop();
            screenPublisher = null;
        }
        if (server !=null){
            server.stop();
            server = null;
        }
    }

    private ISettingsEditor getSettingsEditorByName(String componentName) {
        ISettingsEditor result = null;
        for (ISettingsEditor si : settingsEditorList) {
            result = ISettingsEditorFabric.getSettingsEditorByName(componentName, si);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    private IServer getServer() {
        if (publisherSettings.getLocalAddress() != null && server == null) {

            server = new ServerImpl(publisherSettings.getLocalAddress(), new DirectingMachineImpl());


            IDirectingMachine directingMachine = server.getDirectingMachine();

            directingMachine.setHandler(InetSocketAddress.class, (inetSocketAddress) -> {
                publisherSettings.setRemoteAddress((InetSocketAddress) inetSocketAddress);
                remoteAddressReceived=true;
                try {
                    ISettingsEditor si = getSettingsEditorByName(PublisherSettingsV2.EXPECTED_CHANGES.REMOTEADDRESS);
                    si.updateView();
                } catch (Throwable tr) {
                    BaseOLRC.logError(tr);
                }
                start();
            });

            directingMachine.setHandler(AppType.class, (appType) -> {
                if (appType.equals(publisherSettings.getAppType())) {
                    BaseOLRC.logError(new Exception("Same AppType:" + appType));
                    return;
                }
                BaseOLRC.logInfo("directingMachine.setHandler(AppType.class: launch()");
                start();
            });

            directingMachine.setHandler(NetObject.class, (netObject) -> {
                System.gc();
                NetObject netObject1 = (NetObject) netObject;
                DataKind dataKind = netObject1.dataKind;
                switch (dataKind) {
                    case NET_REMOTE_ADDRESS:
                        directingMachine.direct(netObject1.data);
                        netObject1 = null;
                        break;
                    case APP_TYPE:
                        directingMachine.direct(netObject1.data);
                        netObject1 = null;
                        break;
                    default:
                        directingMachine.direct(netObject1.data);
                        System.out.println("dataKind:" + dataKind);
                        break;
                }
            });
        }
        return server;


    }

    private void createUI() {
        setDefaultLookAndFeelDecorated(true);

        JPanel mainPanel = new JPanel();
        JPanel mainPanelCENTER = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanelCENTER.setLayout(new BoxLayout(mainPanelCENTER, BoxLayout.Y_AXIS));
        mainPanel.add(mainPanelCENTER, BorderLayout.CENTER);
        setContentPane(new JScrollPane(mainPanel));
        settingsEditorList = new ArrayList<>();

        for (IChangesSubscriber item : Arrays.asList(publisherSettings)) {
            ISettingsEditor si = ISettingsEditorFabric.createEditor(item);
            settingsEditorList.add(si);
            JComponent component = si.getSettingsUI();
            mainPanelCENTER.add(item.getName(), component);
        }
        mainPanelCENTER.add(Box.createVerticalGlue());

        JButton buttonLaunch = new JButton("Launch");
        mainPanel.add(buttonLaunch, BorderLayout.NORTH);
        buttonLaunch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launch();
            }
        });

        JButton buttonStart = new JButton("Start");
        mainPanel.add(buttonStart, BorderLayout.EAST);
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BaseOLRC.logInfo("start();");
                start();
            }
        });
        JButton buttonStop = new JButton("Stop");
        mainPanel.add(buttonStop, BorderLayout.WEST);
        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });


    }

    private void updateView() {
        settingsEditorList.forEach(ISettingsEditor::updateView);
    }

    private boolean allSelected(PublisherSettings settings) {
        boolean result = true;
        result = result && settings.getRemoteAddress() != null;
        result = result && settings.getLocalAddress() != null;

        if (result && settings.getAppType() == AppType.PUBLISHER) {
            result = result && settings.getGd() != null;
            result = result && settings.getFps() != null;
        }

        return result;
    }

    private void launch() {
        if (!allSelected(publisherSettings) || (remoteScreenView!=null || screenPublisher!=null)) {
            return;
        }
        if (publisherSettings.getAppType() == AppType.PUBLISHER) {
            launchPublisher();
        } else if (publisherSettings.getAppType() == AppType.MANIPULATOR) {
            launchManipulator();
        }
        BaseOLRC.logInfo("launch() "+publisherSettings.getAppType());
    }

    private void launchPublisher() {

        Rectangle bounds = publisherSettings.getBounds();
        if (bounds == null || Math.min(bounds.width, bounds.height) == 0) {
            bounds = publisherSettings.getGd().getDefaultConfiguration().getBounds();
        }
        IScreenReader screenReader = new ScreenReaderImpl(publisherSettings.getGd(), bounds);

        IScreenPublisher screenPublisher = new ScreenPublisherImpl(
                getClient()
                , screenReader
                , getServer()
                , publisherSettings.getDeviceForParallelCalc()
        );
        screenPublisher.setFPS(publisherSettings.getFps());

        screenPublisher.start();
    }

    private void launchManipulator() {
        IImageCompressor imageCompressor = new ImageCompressorByte();
        imageCompressor.setDeviceForParallelCalc(publisherSettings.getDeviceForParallelCalc());
        RemoteScreenView remoteScreenView = new RemoteScreenView(getServer(), imageCompressor, getClient());

        remoteScreenView.start();
    }


}
