package ol.rc.ui.settingsEditors;

public interface IChangesSubscriber {
    void receiveChanges(String changesName,Object changesValue);
    Property[] getExpectedChanges();
    String getName();
}
