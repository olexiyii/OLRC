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
import ol.rc.ui.settingsEditors.IChangesSubscriber;
import ol.rc.ui.settingsEditors.Property;

import java.awt.*;
import java.net.InetSocketAddress;

/**
 * This class holds settings for Publisher
 * It extends functionality as {@link GeneralSettings} with
 * fields:
 *  - <b>GraphicsDevice gd</b>
 *  - <b>Rectangle bounds</b>
 *  - <b>Integer fps</b>
 *
 * @author Oleksii Ivanov
 */public class PublisherSettingsV2 extends GeneralSettings {
    //the GraphicsDevice for publish by default first GraphicsDevice in system
    protected GraphicsDevice gd;

    //the bounds in GraphicsDevice for publish by default hole GraphicsDevice
    protected Rectangle bounds;

    //the frame per second - speed of publish
    protected Integer fps=10;

    public PublisherSettingsV2() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();

        this.gd = screenDevices[0];
        this.bounds=this.gd.getDefaultConfiguration().getBounds();
    }

    public GraphicsDevice getGd() {
        return gd;
    }
    public Integer getFps() {
        return fps;
    }
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Receive changed value from external object and sets correspond field
     * Implementation of {@link IChangesSubscriber}
     * @param changesName - expects one of {@link EXPECTED_CHANGES} fields value
     * @param changesValue - value received from external object
     */
    @Override
    public void receiveChanges(String changesName, Object changesValue) {
        switch (changesName) {
            case EXPECTED_CHANGES.FPS:
                fps = (Integer) changesValue;
                break;
            case EXPECTED_CHANGES.GRAPHICSDEVICE:
                gd = (GraphicsDevice) changesValue;
                break;
            case EXPECTED_CHANGES.BOUNDS:
                bounds = (Rectangle) changesValue;
                break;
            default:
                super.receiveChanges(changesName, changesValue);
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
                new Property(EXPECTED_CHANGES.BOUNDS,Rectangle.class,bounds,false,this),
                new Property(EXPECTED_CHANGES.REMOTEADDRESS,InetSocketAddress.class,getRemoteAddress(),false,this),
                new Property(EXPECTED_CHANGES.EXTERNALADDRESS,InetSocketAddress.class,getExternalAddress(),false,this),
                new Property(EXPECTED_CHANGES.LOCALADDRESS,InetSocketAddress.class,getLocalAddress(),false,this),
                new Property(EXPECTED_CHANGES.DEVICE,Device.class,deviceForParallelCalc,false,this),

                new Property(EXPECTED_CHANGES.FPS,Integer.class,fps,false,this),
                new Property(EXPECTED_CHANGES.GRAPHICSDEVICE,GraphicsDevice.class,gd,false,this),

        };
    }

    /**
     * Gets name of the object
     * Implementation of {@link IChangesSubscriber}
     */
    @Override
    public String getName() {
        return "Publisher";
    }

    /**
     * static class {@link EXPECTED_CHANGES}  holds names of changes
     */
    public static class EXPECTED_CHANGES extends GeneralSettings.EXPECTED_CHANGES{

//        public static final String REMOTEADDRESS = "Remote address";
//        public static final String EXTERNALADDRESS = "External address";
//        public static final String LOCALADDRESS = "Local address";
//        public static final String DEVICE = "Device for calc";

        public static final String FPS = "FPS";
        public static final String GRAPHICSDEVICE = "Graphics device";
        public static final String BOUNDS = "Bounds";


    }

}
