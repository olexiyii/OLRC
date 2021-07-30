package ol.rc.Image;

import ol.rc.BaseOLRC;
import ol.rc.utils.ImageConverter;
import ol.rc.utils.Packer;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * The implementation of {@link IImageCompressor}
 * uses <code>xor</code> pixel data (<code>byte[]</code>) of ImgResult and ImgSource
 * and pack result <code>byte[]</code>
 */
public class ImageCompressorByte extends BaseOLRC implements IImageCompressor {
    protected Packer packer;
    protected ParallelXORByte kernelCompress;
    private int width;
    private int height;
    private int bytesPerPixel=3;
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

    @Override
    public void setResultArray(byte[] data) {
        previousBuffer=new byte[data.length];
        System.arraycopy(data,0,previousBuffer,0,data.length);
    }

    @Override
    public void setResultImage(BufferedImage resultImage) {
        setImageSettings(resultImage.getWidth(), resultImage.getHeight());

        BufferedImage bufferedImage=ImageConverter.convertTo24Bit(resultImage);
        byte[] tmp = ((DataBufferByte)bufferedImage.getRaster().getDataBuffer()).getData();
        setResultArray(tmp);
    }

    @Override
    public byte[] compress(BufferedImage src) {
        BufferedImage img = ImageConverter.convertTo24Bit(src);
        byte[] tmp = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        return compress(tmp);
    }

    @Override
    public byte[] compress(byte[] img) {
        kernelCompress.setResult(previousBuffer);
        byte[] tmpRes = kernelCompress.process(img);
        previousBuffer=img;
        try {
            return packer.pack(tmpRes);
        } catch (IOException e) {
            logError(e);
        }
        return new byte[0];
    }

    @Override
    public BufferedImage decompress(byte[] data) {
        kernelCompress.setResult(previousBuffer);
        byte[] tmp ;
        byte[] tmpRes= null;
        try {
            tmp = packer.unpack(data);
            tmpRes= kernelCompress.process(tmp);
        } catch (DataFormatException | IOException e) {
            logError(e);
        }
        WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(tmpRes, tmpRes.length), width, height, width * bytesPerPixel, bytesPerPixel, new int[]{2, 1, 0}, null);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        img.setData(raster);
        previousBuffer=tmpRes;


        return img;
    }
    @Override
    public byte[] getResultData(){
        return kernelCompress.getResult();
    }

}
