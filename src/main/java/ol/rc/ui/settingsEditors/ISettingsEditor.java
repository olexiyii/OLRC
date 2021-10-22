package ol.rc.ui.settingsEditors;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.List;

public interface ISettingsEditor extends ActionListener {
    static void add(ISettingsEditor owner,ISettingsEditor added){
        owner.getSettingsUI().add(added.getName(),added.getSettingsUI());
    }
    void setSettingsHolder(Object settingsHolder);
    String getName();
    JComponent getSettingsUI();
    void updateView();
    List<ISettingsEditor> getChieldren();
}
