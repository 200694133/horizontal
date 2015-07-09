package hyn.com.lib;

/**
 * Created by hanyanan on 2015/7/9.
 *
 * A delay get value Reference. In fact when user get current object, it no means that user can get value immediately,
 * when user call {@link #getValue()} method, it's the real operation to get value. It delay load data for resource saving.
 */
public interface DelayValueReference<T> {
    /**
     * This method is a time-consuming operation, it not just return a value, but do the havy work to get the value.
     * For example, when user want a http response, it do the http request operation, so avoid call the function in
     * main thread is necessary.
     * @return the expect value.
     */
    public T getValue();
}
