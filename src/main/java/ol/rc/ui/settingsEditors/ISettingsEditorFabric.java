package ol.rc.ui.settingsEditors;

import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;
import ol.rc.BaseOLRC;
import ol.rc.app.AppType;
import ol.rc.ui.OlPopupMenu;
import ol.rc.ui.renderers.ListCellWordWrap;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.InetSocketAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface ISettingsEditorFabric {
    int DEF_STRUT=5;
    int DEF_COMBOBOX_WIDTH=50;
    static String getUIValueName(String settingsName) {
        return settingsName + "_Value";
    }

    static JComponent getComponentByName(String componentName, JComponent parentComponent) {
        return (JComponent) Arrays.stream(parentComponent.getComponents()).filter(item -> item.getName().equals(componentName)).findFirst().get();
    }
    static JComponent getComponentByName1(String componentName, JComponent parentComponent) {
        JComponent result=null;
        Component[] chiedren=parentComponent.getComponents();
        for (int i=0;i<chiedren.length && result==null;i++){
            if (chiedren[i].getName().equals(componentName)){
                result=(JComponent)chiedren[i];
                break;
            }else{
                result=getComponentByName1(componentName,(JComponent)chiedren[i]);
            }
        }
        return result;
    }
    static ISettingsEditor getSettingsEditorByName(String componentName, ISettingsEditor siParent) {
        ISettingsEditor result=null;

        for (ISettingsEditor si:siParent.getChieldren()){
            if (si.getName().equals(componentName)){
                result=si;
                break;
            }else{
                result=getSettingsEditorByName(componentName,si);
            }
        }
        return result;
    }


    static void setReadOnly(Component component) {
        component.setEnabled(false);
    }

    static void setReadOnly(JComponent component, boolean readOnly) {
        if (!readOnly){
            return;
        }

        Component[] children=component.getComponents();

        if (children==null || children.length==0){
            setReadOnly(component);
        }else {
            for (Component chield:children){
                if (chield instanceof JComponent){
                    setReadOnly((JComponent)chield, readOnly);
                }else {
                    setReadOnly(chield);
                }
            }

        }


        //component.setEnabled(!readOnly);
    }

    static void setReadOnly(ISettingsEditor settingsEditor, boolean readOnly) {
        JComponent component = settingsEditor.getSettingsUI();
        setReadOnly(component, readOnly);
    }

    static ISettingsEditor createEditor(String name, IChangesSubscriber subscriber) {
        ISettingsEditor result = new AbstractSettingsEditor(name, subscriber) {

            List<ISettingsEditor> propertiesList= Collections.EMPTY_LIST;
            @Override
            public List<ISettingsEditor> getChieldren() {
                return propertiesList;
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                subscriber.receiveChanges(name, null);
            }

            @Override
            public JComponent getSettingsUI() {
                if (this.settingsUI == null) {

                    this.settingsUI = new JPanel();
                    settingsUI.setLayout(new BoxLayout(settingsUI, BoxLayout.Y_AXIS));

                    propertiesList=new ArrayList<>();
                    for (Property property : subscriber.getExpectedChanges()) {
                        ISettingsEditor propertyEditor = createEditor(property);
                        propertiesList.add(propertyEditor);

                        settingsUI.add(Box.createVerticalStrut(DEF_STRUT));
                        settingsUI.add(propertyEditor.getName(),propertyEditor.getSettingsUI());
                        setReadOnly(propertyEditor, property.readOnly);
                    }
                    updateView();
                }
                return settingsUI;
            }

            @Override
            public void updateView() {
                for (ISettingsEditor settingsEditor:propertiesList){
                    settingsEditor.updateView();
                }
            }
        };
        return result;
    }

    static ISettingsEditor createEditor(IChangesSubscriber subscriber) {
        return createEditor(subscriber.getName(), subscriber);
    }

    static ISettingsEditor createEditor(Property property) {
        ISettingsEditor propertyEditor = null;
        try {
            propertyEditor = ISettingsEditorFabric.createEditor(property.name, property.clazz, property.value, property.subscriber);
            setReadOnly(propertyEditor, property.readOnly);
        } catch (NoSuchMethodException e) {
            BaseOLRC.logInfo("===NoSuchMethodException===");
            BaseOLRC.logError(e);
        }
        return propertyEditor;
    }

    static ISettingsEditor createEditor(String name, Class clazz, Object objValue, IChangesSubscriber subscriber) throws NoSuchMethodException {
        ISettingsEditor result = null;

        if (clazz.isAssignableFrom(InetSocketAddress.class)) {
            result = createEditorInetSocketAddress(name, (InetSocketAddress) objValue, subscriber);
        } else if (clazz.isAssignableFrom(Integer.class)) {
            result = createEditorInteger(name, (Integer) objValue, subscriber);
        } else if (clazz.isAssignableFrom(Rectangle.class)) {
            result = createEditorRectangle(name, (Rectangle) objValue, subscriber);
        } else if (clazz.isAssignableFrom(Device.class)) {
            result = createEditorDevice(name, (Device) objValue, subscriber);
        } else if (clazz.isAssignableFrom(GraphicsDevice.class)) {
            result = createEditorGraphicsDevice(name, (GraphicsDevice) objValue, subscriber);
        } else if (clazz.isAssignableFrom(AppType.class)) {
            result = createEditorAppType(name, (AppType) objValue, subscriber);
        }

        if (result != null) {
            return result;
        } else {
            throw new NoSuchMethodException("No method for class " + objValue.getClass().getName());
        }

    }

    static ISettingsEditor createEditorInetSocketAddress(String name, InetSocketAddress inetSocketAddress, IChangesSubscriber subscriber) {
        ISettingsEditor result = new AbstractSettingsEditor(name, inetSocketAddress) {
            private JTextField tfIPPort;

            @Override
            public void actionPerformed(ActionEvent evt) {
                String[] ipPort = ((JTextField) evt.getSource()).getText().split(":");
                int port = 0;
                if (ipPort.length > 1) {
                    port = Integer.parseInt(ipPort[1]);
                }

                try {
                    this.setSettingsHolder(new InetSocketAddress(ipPort[0], port));
                    subscriber.receiveChanges(name, settingsHolder);
                } catch (Exception ex) {
                    logError(ex);
                }
            }

            @Override
            public JComponent getSettingsUI() {
                if (this.settingsUI == null) {

                    this.settingsUI = new JPanel();
                    settingsUI.setLayout(new BoxLayout(settingsUI, BoxLayout.X_AXIS));

                    Label label = new Label(getName() + " (Address:Port):");

                    tfIPPort = new JTextField();
                    tfIPPort.addActionListener(this);
                    tfIPPort.addFocusListener(new FocusListener() {
                        String previosValue;

                        @Override
                        public void focusGained(FocusEvent e) {
                            previosValue = ((JTextField) e.getSource()).getText();
                        }

                        @Override
                        public void focusLost(FocusEvent e) {
                            if (!((JTextField) e.getSource()).getText().equals(previosValue)) {
                                ((JTextField) e.getSource()).postActionEvent();
                            }
                        }
                    });
                    Dimension defSize=new Dimension(tfIPPort.getPreferredSize().height *10, tfIPPort.getPreferredSize().height);
                    tfIPPort.setMaximumSize(defSize);
                    tfIPPort.setPreferredSize(defSize);
                    tfIPPort.setMinimumSize(defSize);


                    settingsUI.setMaximumSize(new Dimension(Short.MAX_VALUE, tfIPPort.getPreferredSize().height));
                    settingsUI.add(label);
                    settingsUI.add(Box.createHorizontalStrut(DEF_STRUT));
                    settingsUI.add(name, tfIPPort);

                    JTextField textField=new JTextField("");
                    textField.setEditable(false);
                    textField.setBorder(null);
                    settingsUI.add(textField);

                    tfIPPort.setComponentPopupMenu(OlPopupMenu.getPopup());

                    updateView();
                }
                return settingsUI;
            }

            @Override
            public void updateView() {
                if (settingsHolder==null || !(settingsHolder instanceof InetSocketAddress)){
                    return;
                }

                InetSocketAddress serverSocketAddres = (InetSocketAddress) settingsHolder;
                try {
                    tfIPPort.setText(
                            String.format("%s:%d"
                            , serverSocketAddres.getAddress().getHostAddress()
                            , serverSocketAddres.getPort())
                    );
                } catch (Throwable tr) {
                    logError(tr);
                }
            }
        };
        return result;
    }

    static ISettingsEditor createEditorInteger(String name, Integer intValue, IChangesSubscriber subscriber) {
        ISettingsEditor result = new AbstractSettingsEditor(name, intValue) {
            private JFormattedTextField integerField;

            @Override
            public void actionPerformed(ActionEvent evt) {

                try {
                    int value=Integer.parseInt((String) ((JFormattedTextField) evt.getSource()).getText().trim());
                    this.setSettingsHolder(value);
                    ((JFormattedTextField) evt.getSource()).setValue(this.getSettingsHolder());
                    subscriber.receiveChanges(name, this.getSettingsHolder());
                } catch (Exception ex) {
                    logError(ex);
                }
            }

            @Override
            public JComponent getSettingsUI() {
                if (this.settingsUI == null) {
                    setSettingsHolder(intValue);
                    this.settingsUI = new JPanel();

                    this.settingsUI =new Box(BoxLayout.LINE_AXIS);

                    JLabel label = new JLabel(getName()+":");
                    label.setHorizontalAlignment(SwingConstants.LEFT);

                    NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
                    formatter.setValueClass(Integer.class);
                    formatter.setMinimum(0);
                    formatter.setMaximum(Integer.MAX_VALUE);
                    formatter.setAllowsInvalid(false);
                    formatter.setCommitsOnValidEdit(true);

                    integerField = new JFormattedTextField(formatter);
                    integerField.setHorizontalAlignment(SwingConstants.RIGHT);
                    integerField.addActionListener(this);
                    integerField.addFocusListener(new FocusListener() {
                        String previosValue;

                        @Override
                        public void focusGained(FocusEvent e) {
                            previosValue = ((JFormattedTextField) e.getSource()).getText();
                        }

                        @Override
                        public void focusLost(FocusEvent e) {
                            if (!((JFormattedTextField) e.getSource()).getText().equals(previosValue)) {
                                ((JFormattedTextField) e.getSource()).postActionEvent();
                            }
                        }
                    });

                    int DEF_INTEGER_WIDTH_Local=integerField.getPreferredSize().height*5;
                    Dimension defSize=new Dimension(DEF_INTEGER_WIDTH_Local,integerField.getPreferredSize().height);
                    integerField.setMaximumSize(defSize);
                    integerField.setMinimumSize(defSize);
                    integerField.setPreferredSize(defSize);


                    settingsUI.setMaximumSize(new Dimension(Short.MAX_VALUE,integerField.getPreferredSize().height));
                    settingsUI.add(label);
                    settingsUI.add(Box.createHorizontalStrut(DEF_STRUT));
                    settingsUI.add(integerField);

                    integerField.setComponentPopupMenu(OlPopupMenu.getPopup());

                    updateView();
                }
                return settingsUI;
            }

            @Override
            public void updateView() {
                try {
                    if (settingsHolder!=null){
                        integerField.setValue(settingsHolder);
                    }
                } catch (Throwable tr) {
                    logError(tr);
                }
            }
        };
        return result;
    }


    static ISettingsEditor createEditorDevice(String name, Device device, IChangesSubscriber subscriber) {
        ISettingsEditor result = new AbstractSettingsEditor(name, device) {

            JComboBox<Device> deviceJComboBox;

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    this.setSettingsHolder(deviceJComboBox.getSelectedItem());
                    subscriber.receiveChanges(name, this.settingsHolder);
                } catch (Exception ex) {
                    logError(ex);
                }
            }

            @Override
            public JComponent getSettingsUI() {
                if (this.settingsUI == null) {

                    this.settingsUI = new JPanel();
                    settingsUI.setLayout(new BoxLayout(settingsUI, BoxLayout.X_AXIS));

                    Label label = new Label(getName()+":");

                    deviceJComboBox = new JComboBox<Device>(new DefaultComboBoxModel());
                    for (Device deviceItem : KernelManager.instance().getDefaultPreferences().getPreferredDevices(null)) {
                        deviceJComboBox.addItem(deviceItem);
                    }
                    deviceJComboBox.setMaximumSize(new Dimension(DEF_COMBOBOX_WIDTH,deviceJComboBox.getPreferredSize().height));
                    deviceJComboBox.addActionListener(this);
                    deviceJComboBox.setRenderer(new ListCellWordWrap());

                    JTextField textField=new JTextField("");
                    textField.setEditable(false);
                    textField.setBorder(null);

                    settingsUI.setMaximumSize(new Dimension(Short.MAX_VALUE,deviceJComboBox.getPreferredSize().height));
                    settingsUI.add(label);
                    settingsUI.add(Box.createHorizontalStrut(DEF_STRUT));
                    settingsUI.add(name,deviceJComboBox);
                    settingsUI.add(textField);
                }
                return settingsUI;
            }

            @Override
            public void updateView() {
            }
        };
        return result;
    }

    static ISettingsEditor createEditorGraphicsDevice(String name, GraphicsDevice gd, IChangesSubscriber subscriber) {
        ISettingsEditor result = new AbstractSettingsEditor(name, gd) {
            JComboBox<GraphicsDevice> graphicsDeviceJComboBox;

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    this.setSettingsHolder(graphicsDeviceJComboBox.getSelectedItem());
                    subscriber.receiveChanges(name, this.settingsHolder);
                } catch (Exception ex) {
                    logError(ex);
                }
            }

            @Override
            public JComponent getSettingsUI() {
                if (this.settingsUI == null) {

                    this.settingsUI = new JPanel();
                    settingsUI.setLayout(new BoxLayout(settingsUI, BoxLayout.X_AXIS));

                    Label label = new Label(getName()+":");

                    graphicsDeviceJComboBox= new JComboBox<GraphicsDevice>(new DefaultComboBoxModel());
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice[] screenDevices = ge.getScreenDevices();
                    for (GraphicsDevice gdItem : screenDevices) {
                        graphicsDeviceJComboBox.addItem(gdItem);
                    }

                    graphicsDeviceJComboBox.setMaximumSize(new Dimension(DEF_COMBOBOX_WIDTH,graphicsDeviceJComboBox.getPreferredSize().height));
                    graphicsDeviceJComboBox.addActionListener(this);

                    JTextField textField=new JTextField("");
                    textField.setEditable(false);
                    textField.setBorder(null);

                    settingsUI.setMaximumSize(new Dimension(Short.MAX_VALUE,graphicsDeviceJComboBox.getPreferredSize().height));
                    settingsUI.add(label);
                    settingsUI.add(Box.createHorizontalStrut(DEF_STRUT));
                    settingsUI.add(name, graphicsDeviceJComboBox);
                    settingsUI.add(textField);

                }
                updateView();
                return settingsUI;
            }

            @Override
            public void updateView() {
                graphicsDeviceJComboBox.setSelectedItem(this.settingsHolder);
            }
        };
        return result;
    }

    static ISettingsEditor createEditorRectangle(String name, Rectangle rectangle, IChangesSubscriber subscriber) {
        ISettingsEditor result = new EditorRectangle(name, rectangle, subscriber);
        return result;
    }

    static ISettingsEditor createEditorAppType(String name, AppType appType, IChangesSubscriber subscriber) {
        ISettingsEditor result = new AbstractSettingsEditor(name, appType) {
            JComboBox<AppType> appTypeJComboBox;

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    this.setSettingsHolder(appTypeJComboBox.getSelectedItem());
                    subscriber.receiveChanges(name, this.settingsHolder);
                } catch (Exception ex) {
                    logError(ex);
                }
            }

            @Override
            public JComponent getSettingsUI() {
                if (this.settingsUI == null) {

                    this.settingsUI = new JPanel();
                    settingsUI.setLayout(new BoxLayout(settingsUI, BoxLayout.X_AXIS));

                    Label label = new Label(getName()+":");

                    appTypeJComboBox = new JComboBox<AppType>(new DefaultComboBoxModel());
                    for (AppType appTypeValue : AppType.values()) {
                        appTypeJComboBox.addItem(appTypeValue);
                    }
                    appTypeJComboBox.addActionListener(this);

                    JTextField textField=new JTextField("");
                    textField.setEditable(false);
                    textField.setBorder(null);

                    settingsUI.setMaximumSize(new Dimension(Short.MAX_VALUE,appTypeJComboBox.getPreferredSize().height));
                    settingsUI.add(label);
                    settingsUI.add(Box.createHorizontalStrut(DEF_STRUT));
                    settingsUI.add(name,appTypeJComboBox);
                    settingsUI.add(textField);
                    updateView();
                }
                return settingsUI;
            }

            @Override
            public void updateView() {
                appTypeJComboBox.setSelectedItem(this.settingsHolder);
            }
        };
        return result;
    }

}
