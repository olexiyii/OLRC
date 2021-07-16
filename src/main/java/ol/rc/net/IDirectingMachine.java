package ol.rc.net;

/**
 * The {@link IDirectingMachine} redirect object to handler
 */
public interface IDirectingMachine {

    /**
     * directs <code>obj</code> to {@link IHandler} for <code>obj.getClass()</code>
     * {@link IHandler} for {@link Class} should be sets by <code>setHandler</code> previosly
     *
     * @param obj - object to direct
     */
    void direct(Object obj);

    /**
     * sets {@link IHandler} <code>handler</code> for {@link Class} <code>clazz</code>
     *
     * @param clazz - {@link Class} for which {@link IHandler} <code>handler</code> assigned
     * @param handler - handler for {@link Class} <code>clazz</code>
     */
    void setHandler(Class<?> clazz, IHandler handler);
}
