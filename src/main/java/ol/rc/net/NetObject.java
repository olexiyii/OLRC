package ol.rc.net;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The class {@link NetObject} is a container for transfer data through network
 */
public class NetObject implements Serializable {
    private static final long SerialVersionUID = 1L;
    public DataKind dataKind;
    transient public Object data;

    public static Map<Class, ISerializeWriter> writers=new HashMap<>();
    static {
        writers.put(BufferedImage.class,(out, obj)->{
            try {
                ImageIO.write((BufferedImage) obj,"PNG",out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writers.put(byte[].class,(out, obj)->{
            try {
                out.write((byte[]) obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static NetObject createSCREEN_INITIAL(BufferedImage image){
        return new NetObject(DataKind.SCREEN_INITIAL,image);
    }

    public static NetObject createSCREEN_DIFFERENCES(byte[] differencies){
        return new NetObject(DataKind.SCREEN_DIFFERENCES,differencies);
    }

    //TODO make file transfer
    public static NetObject createKey(byte[] keys){
        return new NetObject(DataKind.KEY,keys);
    }
    public static NetObject createFileName(String fileName){
        return new NetObject(DataKind.FILE_START_NAME,fileName);
    }
    public static NetObject createFileData(byte[] filePart){
        return new NetObject(DataKind.FILE_DATA,filePart);
    }
    public static NetObject createFileFinish(){
        return new NetObject(DataKind.FILE_FINISH,null);
    }
    public static NetObject createOBJECT(Object data){
        return new NetObject(DataKind.OBJECT,data);
    }


    public NetObject(DataKind dataKind, Object data) {
        this.dataKind = dataKind;
        this.data = data;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(data.getClass());

        if (data.getClass()== BufferedImage.class){
            ImageIO.write((BufferedImage) data, "png", out); // png is lossless
        }else if (data.getClass()== byte[].class){
            int length=((byte[]) data).length;
            out.writeInt(length);

            out.write((byte[]) data);
        }else{
            out.writeObject(data);
        }


    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Class clazz= (Class) in.readObject();

        if (clazz == BufferedImage.class){
            data=ImageIO.read(in);
        }else if (clazz== byte[].class){
            int length=in.readInt();
            byte[] dataByte=new byte[length];

            int count=0;

            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            baos.reset();
            while ((count=in.read(dataByte,0,length))>0){
                baos.write(dataByte,0,count);
            }
            baos.close();
            data=baos.toByteArray();
            baos=null;
        }else{
            data=in.readObject();
        }

    }

    private void readObjectNoData() throws ObjectStreamException {
        data=null;
    }
}
