package ol.rc.ui.settingsEditors;

import ol.rc.BaseOLRC;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSettingsEditor extends BaseOLRC implements ISettingsEditor {
    protected JComponent settingsUI;
    protected String name;
    protected Object settingsHolder;

    protected AbstractSettingsEditor(String name){
        super(AbstractSettingsEditor.class);
        this.name=name;
        settingsUI=new JPanel();
        settingsUI.setLayout(new BoxLayout(settingsUI, BoxLayout.Y_AXIS));
    }
    protected AbstractSettingsEditor(String name,Object settingsHolder){
        if (settingsHolder!=null){
            setClass(settingsHolder.getClass());
        }
        this.name=name;
        setSettingsHolder(settingsHolder);
    }
    void add(ISettingsEditor added){
        String nameAdded=added.getName();
        JComponent componentAdded=added.getSettingsUI();
        getSettingsUI().add(nameAdded,componentAdded);
    }
    @Override
    public void setSettingsHolder(Object settingsHolder) {
         this.settingsHolder=settingsHolder;
    }
    public Object getSettingsHolder() {
        return settingsHolder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JComponent getSettingsUI() {
        return settingsUI;
    }

    @Override
    public List<ISettingsEditor> getChieldren() {
        return Collections.EMPTY_LIST;
    }

}
