package ui;

import ol.rc.BaseOLRC;
import ol.rc.app.AppType;
import ol.rc.ui.settingsEditors.IChangesSubscriber;
import ol.rc.ui.settingsEditors.ISettingsEditor;
import ol.rc.ui.settingsEditors.ISettingsEditorFabric;
import ol.rc.ui.settingsEditors.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RunWith(Parameterized.class)
public class PropertyTest extends BaseOLRC {
    Property property=null;
    HashMap<String,Object> resultValues;
    LocalSubscriber localSubscriber;
    List<AppType> appTypeList;

    JFrame frame;

    public PropertyTest(Property property,List<AppType> appTypeList){
        resultValues=new HashMap<String,Object>();
        localSubscriber=new LocalSubscriber(resultValues);
        this.appTypeList=appTypeList;

        this.property=property;
        property.subscriber=localSubscriber;
        localSubscriber.addProperty(property);

    }
    @Parameterized.Parameters
    public static Object[][] parameters1() {
        return new Object[][]{
                {new Property(LocalSubscriber.EXPECTED_CHANGES.APPTYPE, AppType.class, AppType.PUBLISHER,false,null), Arrays.asList(AppType.MANIPULATOR)}
        };
    }
    @Before
    public void init() {

        JFrame frame=new JFrame();
        frame.setLayout(new BorderLayout());
        ISettingsEditor settingsEditor=ISettingsEditorFabric.createEditor(localSubscriber);
        frame.add(settingsEditor.getSettingsUI(),BorderLayout.CENTER);
        frame.setBounds(10,10,500,100);
        frame.show();

    }

    //@Test
    public void testAPPTYPE() {
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    static class LocalSubscriber implements IChangesSubscriber {
        HashMap<String,Object> resultValues;
        List<Property> properties;
        LocalSubscriber(HashMap<String,Object> resultValues){
            this.resultValues=resultValues;
            properties=new ArrayList<>();
        }
        public void addProperty(Property added){
            properties.add(added);
        }

        @Override
        public void receiveChanges(String changesName, Object changesValue) {
            System.out.println(String.format("%s: %s", changesName,changesValue));

            resultValues.put(changesName,changesValue);

        }

        @Override
        public Property[] getExpectedChanges() {
            return (Property[]) properties.toArray(new Property[properties.size()]);
        }

        @Override
        public String getName() {
            return "test";
        }

        private final class EXPECTED_CHANGES {

            public static final String FPS = "FPS";
            public static final String REMOTEADDRESS = "Remote address";
            public static final String LOCALADDRESS = "Local address";
            public static final String APPTYPE = "App type";

            private EXPECTED_CHANGES() {
            }
        }

    }

}
