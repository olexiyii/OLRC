package ol.rc.net;


import ol.rc.BaseOLRC;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * The class {@link NetObject} is a container for transfer data through network
 */
public class NetObject implements Serializable {
    private static final long SerialVersionUID = 1L;
    public static final Map<Class, ISerializeWriter> writers = new HashMap<>();

    static {
        writers.put(BufferedImage.class, (out, obj) -> {
            try {
                ImageIO.write((BufferedImage) obj, "PNG", out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writers.put(byte[].class, (out, obj) -> {
            try {
                out.write((byte[]) obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public DataKind dataKind;
    transient public Object data;

    public NetObject(DataKind dataKind, Object data) {
        this.dataKind = dataKind;
        this.data = data;
    }

    public static NetObject createSCREEN_INITIAL(BufferedImage image) {
        return new NetObject(DataKind.SCREEN_INITIAL, image);
    }

    public static NetObject createSCREEN_DIFFERENCES(byte[] differencies) {
        return new NetObject(DataKind.SCREEN_DIFFERENCES, differencies);
    }

    //TODO make file transfer
    public static NetObject createKey(KeyEvent e) {
        return new NetObject(DataKind.KEY, e);
    }

    public static NetObject createInetSocketAddress(InetSocketAddress inetSocketAddress) {
        return new NetObject(DataKind.NET_REMOTE_ADDRESS, inetSocketAddress);
    }


    public static NetObject createMouse(MouseEvent e) {
        String methodName;//methodName,
        Rectangle srcRectangle = ((JComponent) e.getSource()).getBounds();

        MouseEvent toDataObj = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers()
                , e.getX(), e.getY(), srcRectangle.width, srcRectangle.height
                , e.getClickCount(), e.isPopupTrigger(), e.getButton());
        return new NetObject(DataKind.MOUSE, toDataObj);
    }

    public static NetObject createFileName(String fileName) {
        return new NetObject(DataKind.FILE_START_NAME, fileName);
    }

    public static NetObject createFileData(byte[] filePart) {
        return new NetObject(DataKind.FILE_DATA, filePart);
    }

    public static NetObject createFileFinish() {
        return new NetObject(DataKind.FILE_FINISH, null);
    }

    public static NetObject createOBJECT(Object data) {
        return new NetObject(DataKind.OBJECT, data);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        data = null;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (data==null){
            out.writeObject(null);//Class
            out.writeObject(null);//date
            return;
        }
        out.writeObject(data.getClass());

        if (this.dataKind == DataKind.KEY) {
            out.writeObject(data);
        } else if (this.dataKind == DataKind.MOUSE) {
            out.writeObject(data);
        } else if (this.dataKind == DataKind.NET_REMOTE_ADDRESS) {
            out.writeObject(data);
        } else if (this.dataKind == DataKind.SCREEN_INITIAL) {
            if (data.getClass() == BufferedImage.class) {
                ImageIO.write((BufferedImage) data, "png", out); // png is lossless
            }
        } else if (this.dataKind == DataKind.SCREEN_DIFFERENCES || this.dataKind == DataKind.FILE_DATA) {

            if (data.getClass() == byte[].class) {
                int length = ((byte[]) data).length;
                out.writeInt(length);

                out.write((byte[]) data);
            }
        } else {
            out.writeObject(data);
        }


    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Object obj=new Object();
        Class clazz =null;
        try {
            obj = in.readObject();
            if (obj != null){
                clazz = (Class)obj;
            }else {
                data=null;
                return;
            }
        }catch (ClassCastException e){
            BaseOLRC.logInfo("ClassCastException");
            BaseOLRC.logInfo("readObject");
            BaseOLRC.logInfo(obj.getClass().getName());
            BaseOLRC.logInfo(obj.toString());
        }

        if (this.dataKind == DataKind.KEY) {
            data = in.readObject();
        } else if (this.dataKind == DataKind.MOUSE) {
            data = in.readObject();
        } else if (this.dataKind == DataKind.NET_REMOTE_ADDRESS) {
            data = in.readObject();
        } else if (clazz == BufferedImage.class) {
            data = ImageIO.read(in);
        } else if (clazz == byte[].class) {
            int length = in.readInt();

            byte[] dataByte = new byte[1024];
            int count ;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            baos.reset();
            while ((count = in.read(dataByte, 0, dataByte.length)) > 0) {
                baos.write(dataByte, 0, count);
                length -= count;
            }
            baos.close();
            data = baos.toByteArray();
            baos = null;
            dataByte= null;
        } else {
            data = in.readObject();
        }

    }

    private void readObjectNoData() throws ObjectStreamException {
        data = null;
    }
}
