package ol.rc.net;

import ol.rc.screen.IScreenReader;

/**
 * Publish screen to client
 *
 * @author Oleksii Ivanov
 */
public interface IScreenPublisher {
    IClient getClient();

    void setClient(IClient client);

    IScreenReader getScreenReader();

    void setScreenReader(IScreenReader screenReader);

    void setFPS(int fps);

    int getFPS();
}
