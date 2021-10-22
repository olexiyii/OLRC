package ol.rc.net;

import ol.rc.Image.IImageCompressor;

public interface IRemoteScreenView {
    void setImageCompressor(IImageCompressor imageCompressor);
    void start();
    void stop();
}
