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

package ol.rc.app;

/**
 * This class holds settings for Manipulator
 * Now it have same functionality as {@link GeneralSettings} but logical is separate instance
 *
 * @author Oleksii Ivanov
 */
public class ManipulatorSettings extends GeneralSettings {
    @Override
    public String getName() {
        return "Manupulator";
    }
}
