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

package ol.rc.server;

import ol.rc.BaseOLRC;
import ol.rc.net.IDirectingMachine;
import ol.rc.net.IHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleksii Ivanov
 */

public class DirectingMachineImpl extends BaseOLRC implements IDirectingMachine {
    private final Map<Class, IHandler> handlers;

    public DirectingMachineImpl() {
        super(DirectingMachineImpl.class);
        handlers = new HashMap<>();
    }

    @Override
    public void direct(Object obj) {
        IHandler currentHandler = handlers.get(obj.getClass());
        if (currentHandler==null){
            logger.error("No IHandler for " + obj.getClass());
            return;
        }
        try {
            currentHandler.process(obj);
        } catch (Exception e) {
            error(e);
        }
    }

    @Override
    public void setHandler(Class clazz, IHandler handler) {
        if (handler==null){
            handlers.remove(clazz);
        }else{
            handlers.put(clazz, handler);
        }
    }
}

