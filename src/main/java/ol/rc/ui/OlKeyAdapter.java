package ol.rc.ui;

import ol.rc.BaseOLRC;
import ol.rc.net.IClient;
import ol.rc.net.NetObject;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class OlKeyAdapter extends KeyAdapter {
    private IClient client;
    public OlKeyAdapter(IClient client){
        super();
        setClient(client);

    }
    public void setClient(IClient client) {
        this.client = client;
    }

    private void sendEvent(KeyEvent e){
        //System.out.println(e.toString());
        try {
            client.send(NetObject.createKey(e));
        } catch (IOException ioException) {
            BaseOLRC.logError(ioException);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
//        super.keyTyped(e);
        sendEvent(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        sendEvent(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
        sendEvent(e);
    }
}
