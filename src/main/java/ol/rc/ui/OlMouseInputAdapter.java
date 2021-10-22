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

package ol.rc.ui;

import ol.rc.BaseOLRC;
import ol.rc.net.IClient;
import ol.rc.net.NetObject;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;

/**
 * @author Oleksii Ivanov
 */
public class OlMouseInputAdapter extends MouseInputAdapter {
    IClient client;

    public OlMouseInputAdapter(IClient client) {
        super();
        setClient(client);

    }

    public void setClient(IClient client) {
        this.client = client;
    }

    private void sendEvent(MouseEvent e) {
        try {
            client.send(NetObject.createMouse(e));
        } catch (IOException ioException) {
            BaseOLRC.logError(ioException);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        sendEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        sendEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        sendEvent(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        sendEvent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        sendEvent(e);
    }
}
