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
import ol.rc.app.GeneralSettings;
import ol.rc.publish.IScreenPublisher;
import ol.rc.app.ManipulatorSettings;
import ol.rc.app.PublisherSettings;
import ol.rc.net.ClientImpl;
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
import java.util.Arrays;



/**
 * This class provide user interface to edit available settings and launch
 *  -Publisher of local PC screen or
 *  -Manipulator of remote PC
 *
 *  @author Oleksii Ivanov
 * */

public class Launcher extends JFrame {
    private PublisherSettings publisherSettings;
    private ManipulatorSettings manipulatorSettings;
    private JTabbedPane tabbedPane;

    public Launcher(){
        publisherSettings=new PublisherSettings();
        manipulatorSettings=new ManipulatorSettings();
        createUI();
    }

    private void createUI(){
        setDefaultLookAndFeelDecorated(true);

        JPanel mainPanel=new JPanel();
        mainPanel.setLayout(new BorderLayout());

        tabbedPane=new JTabbedPane(JTabbedPane.TOP,JTabbedPane.WRAP_TAB_LAYOUT);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        mainPanel.add(tabbedPane,BorderLayout.CENTER);
        setContentPane(new JScrollPane(mainPanel));


        for (IChangesSubscriber item: Arrays.asList(publisherSettings,manipulatorSettings)){
            ISettingsEditor si=ISettingsEditorFabric.createEditor(item);
            JComponent component=si.getSettingsUI();
            tabbedPane.addTab(item.getName(), component);
        }

        JButton buttonLaunch=new JButton("Launch");
        mainPanel.add(buttonLaunch,BorderLayout.NORTH);
        buttonLaunch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(publisherSettings.getName())){
                    launchPublisher();
                }else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(manipulatorSettings.getName())){
                    launchManipulator();
                }

            }
        });
    }

    private boolean allSelected(GeneralSettings settings){
        boolean result=true;
        result = result && settings.getRemoteAddress()!=null;
        result = result && settings.getLocalAddress()!=null;

        if (result && settings instanceof PublisherSettings){
            result = result && ((PublisherSettings)settings).getGd()!=null;
            result = result && ((PublisherSettings)settings).getFps()!=null;
        }

        return result;
    }

    private void launchPublisher(){
        if (!allSelected(publisherSettings)){
            return;
        }

        Rectangle bounds=publisherSettings.getBounds();
        if (bounds==null || Math.min(bounds.width,bounds.height)==0){
            bounds=publisherSettings.getGd().getDefaultConfiguration().getBounds();
        }
        IScreenReader screenReader=new ScreenReaderImpl(publisherSettings.getGd(),bounds);
        ClientImpl client = null;
        ServerImpl server = null;
        try {
            client = new ClientImpl(publisherSettings.getRemoteAddress());
            server = new ServerImpl(publisherSettings.getLocalAddress(),new DirectingMachineImpl());
        } catch (IOException e) {
            BaseOLRC.logError(e);
            return;
        }

        IScreenPublisher screenPublisher=new ScreenPublisherImpl(
                client
                ,screenReader
                ,server
                ,publisherSettings.getDeviceForParallelCalc()
            );
        screenPublisher.setFPS(publisherSettings.getFps());

        screenPublisher.start();
    }

    private void launchManipulator(){
        if (!allSelected(manipulatorSettings)){
            return;
        }

        ClientImpl client = null;
        ServerImpl server = null;
        try {
            client = new ClientImpl(manipulatorSettings.getRemoteAddress());
            server = new ServerImpl(manipulatorSettings.getLocalAddress(),new DirectingMachineImpl());
        } catch (IOException e) {
            BaseOLRC.logError(e);
            return;
        }
        IImageCompressor imageCompressor=new ImageCompressorByte();
        imageCompressor.setDeviceForParallelCalc(manipulatorSettings.getDeviceForParallelCalc());
        RemoteScreenView remoteScreenView=new RemoteScreenView(server,imageCompressor,client);

        remoteScreenView.start();
    }


}
