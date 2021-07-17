package ol.rc.Image;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The @{@link IImageСompressor} compress before transfer and decompress after transfer Image
 *  For each implementation should be true for pseudocode:
 *  <code>
 *      decompressor.setImageSettings(img.width, img.height, img.bytesPerPixel);
 *      <b>img</b> equals by pixel to <b>decompressor.decompress(compressor.compress(img));</b>
 *  </code>
 *
 * @author Oleksii Ivanov
 */
public interface IImageСompressor {

    /**
     * Compress image pixel data
     * @param img - Image for compressing
     * @return data of compressed image
     */
    byte[] compress(BufferedImage img) ;
    byte[] compress(byte[] img) ;

    /**
     * Decompress image pixel data and create image, using <code>width, height, bytesPerPixel</code>
     * setted by <code>void setImageSettings(int width,int height,int bytesPerPixel)</code>
     * @param data of compressed image
     * @return Image after decompressing
     */
    BufferedImage decompress(byte[] data) ;

    /**
     * Sets base params of image to restore
     * @param width
     * @param height
     */
    void setImageSettings(int width,int height);
    void setImageSettings(Rectangle bounds);

}
