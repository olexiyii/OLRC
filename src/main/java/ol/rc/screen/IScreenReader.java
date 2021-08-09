package ol.rc.screen;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** The interface to provide ability to get screenshot from specified GraphicsDevice and bounds
 *  @author Oleksii Ivanov
 */

public interface IScreenReader {

    /**
     *  Gets screenshot from specified by <b>setGraphicsDevice()</b> GraphicsDevice and bounds by <b>setBounds()</b>
     * @return screenshot
     */
    BufferedImage getImage();

    /**
     * Specify GraphicsDevice
     * @param gd
     */
    void setGraphicsDevice(GraphicsDevice gd);

    /**
     *
     * @return current GraphicsDevice
     */
    GraphicsDevice getGraphicsDevice();

    /**
     * Specify bounds in GraphicsDevice
     * @param bounds
     */
    void setBounds(Rectangle bounds);

    /**
     * @return current bounds
     */
    Rectangle getBounds();

    boolean isNewSettings();
    void resetNewSettings();

    void receiveMouseEvent(MouseEvent srcEvent);
    void receiveKey(KeyEvent srcEvent);

    public static int[] getDifference(Image img){
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] gs = ge.getScreenDevices();

            Robot robot=new Robot(gs[0]);

            BufferedImage img1=robot.createScreenCapture(gs[0].getDefaultConfiguration().getBounds());
            //img1.getData().
        } catch (AWTException e) {
            e.printStackTrace();
        }
        //img.
        return null;
    }
}
