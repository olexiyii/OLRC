package ol.rc.Image;

import com.aparapi.Kernel;

/**
 * The extension of <code>com.aparapi.Kernel</code> ( the Java implementation OpenCL)
 * for acceleration in ordered images (consecutive frames) differences calculation by using XOR on GPU device.
 * Holds data of current image
 */
class ParallelXORByte extends Kernel {
    private final int initSize = 3 * 3840 * 2160;//4k standard 3840*2160, instead of FullHD 1920*1080 standard;
    private int sizeBuffer = 0;
    /**
     * New image data
     */
    private byte[] imgBuffer = new byte[initSize];//screen size


    /**
     * Current image data
     */
    private byte[] imgResult = new byte[initSize];//screen size

    public ParallelXORByte() {
        //setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
        reset();
    }

    public int getBufferSize() {
        return sizeBuffer;//imgBuffer.length;
    }

    public void reset() {
        sizeBuffer = 0;
    }

    /**
     * Sets <code>imgBuffer</code> in case
     *
     * @param imgBuffer
     */
    public void setImgBuffer(byte[] imgBuffer) {
        sizeBuffer = imgBuffer.length;
        System.arraycopy(imgBuffer, 0, this.imgBuffer, 0, sizeBuffer);
    }

    /**
     * previous imgBuffer is Result on current iteration
     *
     * @param imgBufferParam
     * @return
     */
    public byte[] process(byte[] imgBufferParam) {

        setImgBuffer(imgBufferParam);
        if (sizeBuffer != imgBufferParam.length) {
            setResult(imgBufferParam);
        } else {
            execute(sizeBuffer);
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
        byte[] imgResultRet = new byte[sizeBuffer];
        System.arraycopy(this.imgResult, 0, imgResultRet, 0, sizeBuffer);
        return imgResultRet;
    }

    public void setResult(byte[] imgResult) {
        sizeBuffer = imgResult.length;
        System.arraycopy(imgResult, 0, this.imgResult, 0, sizeBuffer);
    }
}
