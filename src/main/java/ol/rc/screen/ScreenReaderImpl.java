package ol.rc.screen;

import ol.rc.BaseOLRC;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

/**
 * @author Oleksii Ivanov
 */

public class ScreenReaderImpl extends BaseOLRC implements IScreenReader {
    private Robot robot;
    private boolean flagNewSettings;

    private GraphicsDevice graphicsDevice;
    private Rectangle bounds;


    private ScreenReaderImpl() {
    }

    public ScreenReaderImpl(GraphicsDevice gd, Rectangle bounds) {
        super(ScreenReaderImpl.class);
        setBounds(bounds);
        setGraphicsDevice(gd);
    }

    @Override
    public BufferedImage getImage() {

        return robot.createScreenCapture(bounds);
    }

    @Override
    public GraphicsDevice getGraphicsDevice() {
        return graphicsDevice;
    }

    @Override
    public void setGraphicsDevice(GraphicsDevice gd) {
        graphicsDevice = gd;
        flagNewSettings = true;
        try {
            robot = new Robot(graphicsDevice);
        } catch (AWTException e) {
            logError(e);
        }
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Rectangle bounds) {
        flagNewSettings = true;
        this.bounds = bounds;
    }

    @Override
    public boolean isNewSettings() {
        return flagNewSettings;
    }

    @Override
    public void resetNewSettings() {
        flagNewSettings = false;
    }

    @Override
    public void setNewSettings() {
        flagNewSettings = true;
    }

    private int mouseButtonConvertToInputEvent(MouseEvent srcEvent) {
        int buttonMask = 0;
        int mouseEventButton = srcEvent.getButton();
        if (mouseEventButton == 1) {
            buttonMask = buttonMask
                    | InputEvent.BUTTON1_MASK
                    | InputEvent.BUTTON1_DOWN_MASK;
        } else if (mouseEventButton == 2) {
            buttonMask = buttonMask
                    | InputEvent.BUTTON2_MASK
                    | InputEvent.BUTTON2_DOWN_MASK;
        } else if (mouseEventButton == 3) {
            buttonMask = buttonMask
                    | InputEvent.BUTTON3_MASK
                    | InputEvent.BUTTON3_DOWN_MASK;
        }
        buttonMask |= srcEvent.isAltDown() ? InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK : 0;

        buttonMask |= srcEvent.isAltGraphDown() ? InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.ALT_MASK : 0;

        buttonMask |= srcEvent.isControlDown() ? InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK : 0;

        buttonMask |= srcEvent.isShiftDown() ? InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK : 0;

        buttonMask |= srcEvent.isMetaDown() ? InputEvent.META_DOWN_MASK | InputEvent.META_MASK : 0;


        return buttonMask;
    }

    private Point getScreenPoint(MouseEvent srcEvent) {
        double wScale = 1.0 * getBounds().width / srcEvent.getXOnScreen();
        double hScale = 1.0 * getBounds().height / srcEvent.getYOnScreen();

        //coords in bounds
        int x = (int) (srcEvent.getX() * wScale);
        int y = (int) (srcEvent.getY() * hScale);

        //coords on screen
        x += getBounds().x;
        y += getBounds().y;

        return new Point(x, y);
    }

    @Override
    public void receiveMouseEvent(MouseEvent srcEvent) {

        Point screenPoint = getScreenPoint(srcEvent);
        int x = screenPoint.x;
        int y = screenPoint.y;
        int buttonInputEvent = mouseButtonConvertToInputEvent(srcEvent);
        if (srcEvent.getID() == MouseEvent.MOUSE_CLICKED) {
            robot.mouseMove(x, y);
            robot.mousePress(buttonInputEvent);
            robot.mouseRelease(buttonInputEvent);
        } else if (srcEvent.getID() == MouseEvent.MOUSE_PRESSED) {
            robot.mouseMove(x, y);
            robot.mousePress(buttonInputEvent);
        } else if (srcEvent.getID() == MouseEvent.MOUSE_RELEASED) {
            robot.mouseMove(x, y);
            robot.mouseRelease(buttonInputEvent);
            //logInfo("MOUSE_RELEASED");
        } else if (srcEvent.getID() == MouseEvent.MOUSE_MOVED) {
            robot.mouseMove(x, y);
        } else if (srcEvent.getID() == MouseEvent.MOUSE_WHEEL) {
            //mouseWheelMoved
            robot.mouseMove(x, y);
            MouseWheelEvent we = (MouseWheelEvent) srcEvent;
            robot.mouseWheel(we.getWheelRotation());

        }


    }

    @Override
    public void receiveKey(KeyEvent srcEvent) {
        if (srcEvent.getID() == KeyEvent.KEY_PRESSED) {
            robot.keyPress(srcEvent.getKeyCode());
        } else if (srcEvent.getID() == KeyEvent.KEY_RELEASED) {
            robot.keyRelease(srcEvent.getKeyCode());
        }

    }
}
