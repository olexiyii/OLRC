package ol.rc.server;

import ol.rc.BaseOLRC;
import ol.rc.net.IDirectingMachine;
import ol.rc.net.IHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Oleksii Ivanov
 */

public class DirectingMachineImpl extends BaseOLRC implements IDirectingMachine {
    Map<Class, IHandler> handlers;

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
        handlers.put(clazz, handler);
    }
}

