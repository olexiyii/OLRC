package ol.rc.Image;

import com.aparapi.Kernel;

/**
 * The extension of <code>com.aparapi.Kernel</code> ( the Java implementation OpenCL)
 * for acceleration in ordered images (consecutive frames) differences calculation by using XOR on GPU device.
 * Holds data of current image
 */
class ParallelXORByte extends Kernel {

    private final int initSize = 3 * 1920 * 1080;
    /**
     * New image data
     */
    //private byte[] imgBuffer = new byte[initSize];//screen size
    private byte[] imgBuffer;// = new byte[initSize];//screen size

    /**
     * Current image data
     */
//    private byte[] imgResult = new byte[initSize];//screen size
    private byte[] imgResult;// = new byte[initSize];//screen size

    public ParallelXORByte() {
        setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
        reset();
    }

    public int getBufferSize() {
        return imgBuffer.length;
    }

    public void reset() {
        imgResult = new byte[0];
    }

    /**
     * Sets <code>imgBuffer</code> in case
     *
     * @param imgBuffer
     */
    public void setImgBuffer(byte[] imgBuffer) {
        this.imgBuffer = imgBuffer;
    }

    /**
     * @param imgBuffer
     * @return
     */
    public byte[] process(byte[] imgBuffer) {
        setImgBuffer(imgBuffer);
        if (this.imgBuffer.length != imgResult.length) {
            //
            //imgResult = new byte[imgBuffer.length];
            imgResult = imgBuffer;
        } else {
            execute(imgBuffer.length);
        }

        return getResult();
    }

    /**
     * That code runs on GPU (by using OpenCL)
     */
    @Override
    public void run() {
        int gid = getGlobalId();
        imgResult[gid] = (byte) (imgResult[gid] ^ imgBuffer[gid]);
    }

    public byte[] getResult() {
        return imgResult;
    }
}
