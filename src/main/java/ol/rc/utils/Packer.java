package ol.rc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utility class for packing(byte[] or int[])/unpacking(byte[])
 *
 * @author Oleksii Ivanov
 */
public class Packer {
    byte[] buffer = new byte[1024 * 1024];
    private Deflater compressor;
    private Inflater decompressor;
    private ByteArrayOutputStream baos;

    public Packer() {
        decompressor = new Inflater();
        compressor = new Deflater();
        baos = new ByteArrayOutputStream();

    }

    public void setCompressorLevel(int level) {
        compressor.setLevel(level);
    }

    public byte[] pack(byte[] input) throws IOException {

        compressor.reset();
        baos.reset();
        setCompressorLevel(Deflater.BEST_SPEED);
        compressor.setInput(input);
        compressor.finish();


        while (!compressor.finished()) {
            int count = compressor.deflate(buffer);
            baos.write(buffer, 0, count);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    public byte[] unpack(byte[] input) throws IOException, DataFormatException {
        decompressor.reset();
        baos.reset();

        decompressor.setInput(input);

        int count;
        while (!decompressor.finished() && (count = decompressor.inflate(buffer)) > 0) {
            baos.write(buffer, 0, count);
        }

        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    public byte[] pack(int[] input) throws IOException {
        return pack(ByteToInt.intArrToByteArr(input));
    }

    public byte[] unpack(int[] input) throws IOException, DataFormatException {
        return unpack(ByteToInt.intArrToByteArr(input));
    }

}
