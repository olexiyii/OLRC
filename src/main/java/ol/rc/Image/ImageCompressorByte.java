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
import ol.rc.BaseOLRC;
import ol.rc.utils.ImageConverter;
import ol.rc.utils.Packer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.zip.DataFormatException;

/**
 * The implementation of {@link IImageCompressor}
 * uses <code>xor</code> pixel data (<code>byte[]</code>) of ImgResult and ImgSource
 * and pack result <code>byte[]</code>
 *
 * @author Oleksii Ivanov
 */
public class ImageCompressorByte extends BaseOLRC implements IImageCompressor {
    protected final Packer packer;

    //quick XOR processor
    protected final ParallelXORByte kernelCompress;

    private int width;
    private int height;
    private final int bytesPerPixel = 3;

    //previous image data
    private byte[] previousBuffer;// = new byte[initSize];//screen size


    public ImageCompressorByte() {
        super(ImageCompressorByte.class);
        packer = new Packer();
        kernelCompress = new ParallelXORByte();
    }


    @Override
    public void setImageSettings(int width, int height) {
        this.height = height;
        this.width = width;
        kernelCompress.reset();
    }

    @Override
    public void setImageSettings(Rectangle bounds) {
        setImageSettings(bounds.width, bounds.height);
    }

    //sets previousBuffer it should be alight by bytesPerPixel
    @Override
    public void setResultArray(byte[] data) {
        previousBuffer = new byte[data.length];
        System.arraycopy(data, 0, previousBuffer, 0, data.length);
    }

    //sets image data, alights it by bytesPerPixel and sets previousBuffer
    @Override
    public void setResultImage(BufferedImage resultImage) {
        setImageSettings(resultImage.getWidth(), resultImage.getHeight());

        BufferedImage bufferedImage = ImageConverter.convertTo24Bit(resultImage);
        byte[] tmp = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        setResultArray(tmp);
    }

    @Override
    public void setDeviceForParallelCalc(Device deviceForParallelCalc) {
        kernelCompress.setDevice(deviceForParallelCalc);
    }

    //compress image data
    @Override
    public byte[] compress(BufferedImage src) {
        BufferedImage img = ImageConverter.convertTo24Bit(src);
        byte[] tmp = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        return compress(tmp);
    }

    //compress image data
    @Override
    public byte[] compress(byte[] img) {
        kernelCompress.setResult(previousBuffer);
        byte[] tmpRes = kernelCompress.process(img);
        previousBuffer = img;
        try {
            return packer.pack(tmpRes);
        } catch (IOException e) {
            logError(e);
        }
        return new byte[0];
    }

    //decompress image data
    @Override
    public BufferedImage decompress(byte[] data) {
        kernelCompress.setResult(previousBuffer);
        byte[] tmpRes = null;
        try {
            WeakReference ws = new WeakReference(packer.unpack(data));
            tmpRes = kernelCompress.process((byte[]) ws.get());
            ws.clear();
        } catch (DataFormatException | IOException e) {
            logError(e);
        }
        WeakReference ws = new WeakReference(new DataBufferByte(tmpRes, tmpRes.length));
        WeakReference ws1 = new WeakReference(Raster.createInterleavedRaster((DataBufferByte) ws.get(), width, height, width * bytesPerPixel, bytesPerPixel, new int[]{2, 1, 0}, null));
        WritableRaster raster = (WritableRaster) ws1.get();


        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        img.setData(raster);
        ws.clear();
        ws1.clear();
        ws = null;
        ws1 = null;
        previousBuffer = tmpRes;


        return img;
    }

    @Override
    public byte[] getResultData() {
        return kernelCompress.getResult();
    }

}
