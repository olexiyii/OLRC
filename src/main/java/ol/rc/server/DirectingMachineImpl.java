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
    public Stack<Object> stackHandlerData;
    Map<Class, IHandler> handlers;
    // TODO: 16.07.2021  remove stackHandlerData after testing

    public DirectingMachineImpl() {
        super(DirectingMachineImpl.class);
        handlers = new HashMap<>();
        stackHandlerData = new Stack<>();
    }

    @Override
    public void direct(Object obj) {
        IHandler currentHandler = handlers.get(obj.getClass());
        try {
            currentHandler.process(obj);
            stackHandlerData.push(obj);
        } catch (Exception e) {
            logger.error("No IHandler for " + obj.getClass());
            error(e);
        }
    }

    @Override
    public void setHandler(Class clazz, IHandler handler) {
        handlers.put(clazz, handler);
    }
}

