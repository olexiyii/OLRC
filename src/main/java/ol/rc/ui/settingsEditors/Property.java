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

/**
 * @author Oleksii Ivanov
 */
public class Property {
    public String name;
    public Object value;
    public boolean readOnly;
    public IChangesSubscriber subscriber;
    public Class clazz;
    public Property(String name,Class clazz, Object value, boolean readOnly,IChangesSubscriber subscriber) {
        this(name, value, readOnly,subscriber);
        this.clazz=clazz;
    }

    private Property(String name, Object value, boolean readOnly,IChangesSubscriber subscriber) {
        this(name, value, readOnly);
        this.subscriber=subscriber;
    }

    private Property(String name, Object value, boolean readOnly) {
        this.name = name;
        this.value = value;
        this.readOnly = readOnly;
    }
    public Property() {
    }
}
