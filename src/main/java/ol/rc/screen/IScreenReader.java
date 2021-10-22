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

package ol.rc.screen;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** The interface to provide ability to get screenshot from specified GraphicsDevice and bounds
 *  @author Oleksii Ivanov
 */

public interface IScreenReader {

    /**
     *  Gets screenshot from specified by <b>setGraphicsDevice()</b> GraphicsDevice and bounds by <b>setBounds()</b>
     * @return screenshot
     */
    BufferedImage getImage();

    /**
     * Specify GraphicsDevice
     * @param gd - the new GraphicsDevice for taking screenshots
     */
    void setGraphicsDevice(GraphicsDevice gd);

    /**
     *
     * @return current GraphicsDevice
     */
    GraphicsDevice getGraphicsDevice();

    /**
     * Specify bounds in GraphicsDevice
     * @param bounds - the screenshots bounds in GraphicsDevice
     */
    void setBounds(Rectangle bounds);

    /**
     * @return current bounds
     */
    Rectangle getBounds();

    boolean isNewSettings();
    void resetNewSettings();
    void setNewSettings();

    void receiveMouseEvent(MouseEvent srcEvent);
    void receiveKey(KeyEvent srcEvent);

}
