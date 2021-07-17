package ol.rc.Image;

import ol.rc.BaseOLRC;
import ol.rc.utils.Packer;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * The implementation of {@link IImageСompressor}
 * uses <code>xor</code> pixel data (<code>byte[]</code>) of ImgResult and ImgSource
 * and pack result <code>byte[]</code>
 */
public class ImageCompressorByte extends BaseOLRC implements IImageСompressor {
    protected Packer packer;
    protected ParallelXORByte kernelCompress;
    protected ParallelXORByte kernelDecompress;
    private int width;
    private int height;
    private int bytesPerPixel=3;

    public ImageCompressorByte() {
        super(ImageCompressorByte.class);
        packer = new Packer();
        kernelCompress = new ParallelXORByte();
        kernelDecompress = new ParallelXORByte();
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
    public byte[] compress(BufferedImage src) {
        BufferedImage img = convertTo24Bit(src);
        byte[] tmp = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        return compress(tmp);
    }

    @Override
    public byte[] compress(byte[] img) {
        byte[] tmpRes = kernelCompress.process(img);
//        byte[] tmpRes=new byte[tmpRes1.length];
//        System.arraycopy(tmpRes1,0,tmpRes,0,tmpRes1.length);
        try {
            return packer.pack(tmpRes);
        } catch (IOException e) {
            logError(e);
        }
        return new byte[0];
    }

    @Override
    public BufferedImage decompress(byte[] data) {
        byte[] tmp = null;
        try {
            tmp = packer.unpack(data);
        } catch (DataFormatException | IOException e) {
            logError(e);
        }
        WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(tmp, tmp.length), width, height, width * bytesPerPixel, bytesPerPixel, new int[]{0, 1, 2}, null);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        img.setData(raster);

        return img;
    }

    public BufferedImage convertTo24Bit(BufferedImage src) {
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
