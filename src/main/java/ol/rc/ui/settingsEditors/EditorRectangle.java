/*
 * Copyright (c) 2021. Oleksii Ivanov
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package ol.rc.ui.settingsEditors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;

/**
 * @author Oleksii Ivanov
 */
class EditorRectangle extends AbstractSettingsEditor implements IChangesSubscriber{
    private final IChangesSubscriber subscriber;
    private Rectangle rectangle;
    public EditorRectangle(String name, Rectangle rectangle, IChangesSubscriber subscriber){
        super(name);
        this.rectangle=rectangle;
        this.subscriber=subscriber;
        this.settingsUI=null;
    }
    public Rectangle getRectangle(){
        if (rectangle==null){
            rectangle=new Rectangle(0,0,0,0);
        }
        return rectangle;
    }
    @Override
    public void receiveChanges(String changesName, Object changesValue) {
        try {
            Field field=Rectangle.class.getField(changesName);
            field.set(getRectangle(),changesValue);
            actionPerformed(null);
        } catch (NoSuchFieldException e) {
            logError(e);
        } catch (IllegalAccessException e) {
            logError(e);
        }
    }

    @Override
    public Property[] getExpectedChanges() {
        return new Property[]{
                new Property("x",Integer.class,getRectangle().x,true,this),
                new Property("y",Integer.class,getRectangle().y,false,this),
                new Property("width",Integer.class,getRectangle().width,false,this),
                new Property("height",Integer.class,getRectangle().height,false,this),
        };
    }

    @Override
    public void updateView() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        subscriber.receiveChanges(name, getRectangle());
    }
    @Override
    public JComponent getSettingsUI() {
        if (this.settingsUI==null) {
            this.settingsUI = new JPanel();
            settingsUI.setLayout(new BoxLayout(settingsUI, BoxLayout.X_AXIS));

            JTextField textField=new JTextField("");
            textField.setEditable(false);
            textField.setBorder(null);


            Label label = new Label(getName()+":");
            settingsUI.add(label);
            settingsUI.add(Box.createHorizontalStrut(ISettingsEditorFabric.DEF_STRUT));
            for (Property property:getExpectedChanges()){
                settingsUI.add(ISettingsEditorFabric.createEditor(property).getSettingsUI());
                settingsUI.add(Box.createHorizontalStrut(ISettingsEditorFabric.DEF_STRUT*2));
            }
            settingsUI.add(textField);
            settingsUI.setMaximumSize(new Dimension(Short.MAX_VALUE,textField.getPreferredSize().height));

        }
        return settingsUI;
    }
}
