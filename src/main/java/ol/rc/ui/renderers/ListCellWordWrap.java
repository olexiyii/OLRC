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

package ol.rc.ui.renderers;

import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;

/**
 * @author Oleksii Ivanov
 */
public class ListCellWordWrap implements ListCellRenderer<Device> {

    private final JTextField tf;

    public ListCellWordWrap() {
        tf = new JTextField();
        tf.setBorder(null);
    }
    @Override
    public Component getListCellRendererComponent(JList<? extends Device> list, Device value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected){
            tf.setBackground(Color.GREEN);
        }else{
            tf.setBackground(Color.LIGHT_GRAY);
        }
        tf.setText(value.getShortDescription());
        return tf;
    }
}