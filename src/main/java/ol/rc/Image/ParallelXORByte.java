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

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;
import ol.rc.BaseOLRC;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * The extension of <code>com.aparapi.Kernel</code> ( the Java implementation OpenCL)
 * for acceleration in ordered images (consecutive frames) differences calculation by using XOR on GPU device.
 * Holds data of current image
 *
 * @author Oleksii Ivanov
 */
class ParallelXORByte extends Kernel {
    //4k standard 3840*2160, instead of FullHD 1920*1080 standard;
    //3 - the bytes per pixel (rgb)
    private final int initSize = 3 * 3840 * 2160;
    private int sizeBuffer = 0;

    private Device device;
    /**
     * New image data
     */
    private final byte[] imgBuffer = new byte[initSize];//screen size
    private final int[] count=new int[1];


    /**
     * Current image data
     */
    private final byte[] imgResult = new byte[initSize];//screen size

    public ParallelXORByte() {
        reset();
        setDevice(KernelManager.instance().bestDevice());
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
        KernelManager
                .instance()
                .setDefaultPreferredDevices(
                        new LinkedHashSet<>(Arrays.asList(new Device[] {this.device}))
                );
        BaseOLRC.logInfo("============");
        BaseOLRC.logInfo(this.device.getShortDescription());
    }

    public int getBufferSize() {
        return sizeBuffer;
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
            count[0]=0;
            Range range=device.createRange(sizeBuffer);

            try {
                execute(range);
            }catch (Throwable tr){
                BaseOLRC.logInfo("ERROR execute(range);");
                BaseOLRC.logError(tr);
            }
//            BaseOLRC.logInfo("non Zero="+count[0]);
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
        if (imgResult[gid]!=0){
            count[0]++;
        }
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
