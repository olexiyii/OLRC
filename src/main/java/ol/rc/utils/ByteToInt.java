package ol.rc.utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * The utility to shots code for convert byte[] <=> int[]
 *
 * @author Oleksii Ivanov
 */
public final class ByteToInt {
    private ByteToInt() {
    }

    public static byte[] intArrToByteArr(int[] input) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(input.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(input);

        return byteBuffer.array();
    }

    public static int[] byteArrToIntArr(byte[] input) {
        return ByteBuffer.wrap(input).asIntBuffer().array();
    }
}
