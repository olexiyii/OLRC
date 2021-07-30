package ol.rc.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

public class ImageConverter {
    public static BufferedImage convertTo24Bit(BufferedImage src) {
        DataBuffer buff = src.getRaster().getDataBuffer();
        if (src.getColorModel().getPixelSize() == 24 && buff instanceof DataBufferByte) {
            return src;
        }
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return img;
    }
}
