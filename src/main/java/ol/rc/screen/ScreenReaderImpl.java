package ol.rc.screen;

import ol.rc.BaseOLRC;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Oleksii Ivanov
 */

public class ScreenReaderImpl extends BaseOLRC implements IScreenReader{
    private Robot robot;
    private boolean flagNewSettings;

    private GraphicsDevice graphicsDevice;
    private Rectangle bounds;

    private ScreenReaderImpl(){
    }
    public ScreenReaderImpl(GraphicsDevice gd,Rectangle bounds){
        super(ScreenReaderImpl.class);
        setBounds(bounds);
        setGraphicsDevice(gd);
    }

    @Override
    public BufferedImage getImage() {
        return robot.createScreenCapture(bounds);
    }

    @Override
    public void setGraphicsDevice(GraphicsDevice gd) {
        graphicsDevice=gd;
        flagNewSettings=true;
        try {
            robot=new Robot(graphicsDevice);
        } catch (AWTException e) {
            logError(e);
        }
    }

    @Override
    public GraphicsDevice getGraphicsDevice() {
        return graphicsDevice;
    }

    @Override
    public void setBounds(Rectangle bounds) {
        flagNewSettings=true;
        this.bounds=bounds;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public boolean isNewSettings() {
        return flagNewSettings;
    }

    @Override
    public void resetNewSettings() {
        flagNewSettings=false;
    }
}
