package ol.rc.ui;

import javax.swing.*;

public interface ISettingsEditor {
    String getName();
    JComponent getSettingsUI();
    void updateView();

}
