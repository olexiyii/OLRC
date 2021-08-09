package ol.rc.ui;

import ol.rc.BaseOLRC;
import ol.rc.net.IClient;
import ol.rc.net.NetObject;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;

public class OlMouseInputAdapter extends MouseInputAdapter {
    IClient client;

    public OlMouseInputAdapter(IClient client){
        super();
        setClient(client);

    }

    public void setClient(IClient client) {
        this.client = client;
    }

    private void sendEvent(MouseEvent e){

        try {
            client.send(NetObject.createMouse(e));
        } catch (IOException ioException) {
            BaseOLRC.logError(ioException);
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        //sendEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        sendEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        sendEvent(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        sendEvent(e);

    }
    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        sendEvent(e);
    }
}
