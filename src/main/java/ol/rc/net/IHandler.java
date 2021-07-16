package ol.rc.net;

/**
 * The {@link IHandler} process object
 */
public interface IHandler<T> {
    void process(T obj);
}
