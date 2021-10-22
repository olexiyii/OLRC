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

package ol.rc.Image;

import com.aparapi.device.Device;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The @{@link IImageCompressor} compress before transfer and decompress after transfer Image
 *  For each implementation should be true for pseudocode:
 *  <code>
 *      decompressor.setImageSettings(img.width, img.height, img.bytesPerPixel);
 *      <b>img</b> equals by pixel to <b>decompressor.decompress(compressor.compress(img));</b>
 *  </code>
 *
 * @author Oleksii Ivanov
 */
public interface IImageCompressor {

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
     */
    void setImageSettings(int width,int height);
    void setImageSettings(Rectangle bounds);
    void setResultArray(byte[] data);
    void setResultImage(BufferedImage resultImage);
    void setDeviceForParallelCalc(Device deviceForParallelCalc);

    byte[] getResultData();

}
