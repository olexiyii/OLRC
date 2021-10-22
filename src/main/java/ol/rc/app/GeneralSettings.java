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

import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;
import ol.rc.ui.settingsEditors.IChangesSubscriber;
import ol.rc.ui.settingsEditors.Property;
import ol.rc.utils.JSTUNClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class {@link GeneralSettings} holds general settings for Publisher and Manipulator modes
 * first of all is a network settings (<b>localAddress</b>  and <b>remoteAddress</b> and <b>externalAddress</b>)
 *
 * @author Oleksii Ivanov</>
 * @see JSTUNClient
 */
public class GeneralSettings implements IChangesSubscriber {
    //type off launch (PUBLISHER,MANIPULATOR)
    protected AppType appType;
    // connectionData - gets from STUN server  using class JSTUNClient
    protected JSTUNClient.ConnectionData connectionData;
    // local address of current PC
    protected InetSocketAddress localAddress;
    // external address of current PC
    protected InetSocketAddress externalAddress;
    // remote PC address
    protected InetSocketAddress remoteAddress;

    // OpenCL Device for screen image compressing/decompressing
    protected Device deviceForParallelCalc;

    public GeneralSettings() {
        appType = AppType.MANIPULATOR;
        deviceForParallelCalc = getDeviceForParallelCalc();
    }

    /**
     * Gets STUN servers addresses from "stunServers.txt"
     *
     * @return list of {@link InetSocketAddress}
     */
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

    /**
     * Get connection data from STUN server
     *
     * @return {@link JSTUNClient.ConnectionData}
     * @pattern Lazy initialization
     */
    private JSTUNClient.ConnectionData getConnectionData() {
        if (connectionData == null) {
            connectionData = JSTUNClient.getExtermalConnectionData(getSTUNServers());
        }
        return connectionData;
    }

    public void resetConnectionData() {
        connectionData = null;
        localAddress = null;
        externalAddress = null;
    }

    /**
     * Get local address
     *
     * @return {@link InetSocketAddress} local address
     * @pattern Lazy initialization
     */
    public InetSocketAddress getLocalAddress() {
        if (localAddress == null) {
            setLocalAddress(new InetSocketAddress(getConnectionData().datagramSocket.getLocalAddress(), getConnectionData().datagramSocket.getLocalPort()));
        }
        return localAddress;
    }

    protected void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Get external address
     *
     * @return {@link InetSocketAddress} external address
     * @pattern Lazy initialization
     */
    public InetSocketAddress getExternalAddress() {
        if (externalAddress == null) {

            setExternalAddress(new InetSocketAddress(getConnectionData().address, getConnectionData().port));
        }
        return externalAddress;
    }

    protected void setExternalAddress(InetSocketAddress externalAddress) {
        this.externalAddress = externalAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }


    public Device getDeviceForParallelCalc() {
        if (deviceForParallelCalc == null) {
            deviceForParallelCalc = KernelManager.instance().bestDevice();
        }
        return deviceForParallelCalc;
    }

    public AppType getAppType() {
        return appType;
    }


    /**
     * Receive changed value from external object and sets correspond field
     * Implementation of {@link IChangesSubscriber}
     *
     * @param changesName  - expects one of {@link EXPECTED_CHANGES} fields value
     * @param changesValue - value received from external object
     */
    @Override
    public void receiveChanges(String changesName, Object changesValue) {
        switch (changesName) {
            case EXPECTED_CHANGES.REMOTEADDRESS:
                setRemoteAddress((InetSocketAddress) changesValue);
                break;
            case EXPECTED_CHANGES.EXTERNALADDRESS:
                setExternalAddress((InetSocketAddress) changesValue);
                break;
            case EXPECTED_CHANGES.LOCALADDRESS:
                setLocalAddress((InetSocketAddress) changesValue);
                break;
            case EXPECTED_CHANGES.DEVICE:
                deviceForParallelCalc = (Device) changesValue;
                break;
            case EXPECTED_CHANGES.APPTYPE:
                appType = (AppType) changesValue;
                break;
        }

    }

    /**
     * Gets {@link Property}[] - array of properties to edit
     * Implementation of {@link IChangesSubscriber}
     */
    @Override
    public Property[] getExpectedChanges() {
        return new Property[]{
                new Property(EXPECTED_CHANGES.APPTYPE, AppType.class, appType, false, this),
                new Property(EXPECTED_CHANGES.REMOTEADDRESS, InetSocketAddress.class, getRemoteAddress(), false, this),
                new Property(EXPECTED_CHANGES.EXTERNALADDRESS, InetSocketAddress.class, getExternalAddress(), false, this),
                new Property(EXPECTED_CHANGES.LOCALADDRESS, InetSocketAddress.class, getLocalAddress(), false, this),
                new Property(EXPECTED_CHANGES.DEVICE, Device.class, getDeviceForParallelCalc(), false, this),
        };
    }

    /**
     * Gets name of the object
     * Implementation of {@link IChangesSubscriber}
     */
    @Override
    public String getName() {
        return "GeneralSettings";
    }

    /**
     * static class {@link EXPECTED_CHANGES}  holds names of changes
     */
    protected static class EXPECTED_CHANGES {


        public static final String APPTYPE = "App type";
        public static final String REMOTEADDRESS = "Remote address";
        public static final String EXTERNALADDRESS = "External address";
        public static final String LOCALADDRESS = "Local address";
        public static final String DEVICE = "Device for calc";

    }

}
