package eye.restul.server;

import java.util.concurrent.Callable;
import eye.restul.server.ObjectReference;

public class LazyReference<T> implements ObjectReference<T> {
    
    private Callable<T> getter;
    
    private boolean initialized = false;
    
    private T target;
    
    public LazyReference(Callable<T> getter) {
        this.getter = getter;
    }

    public void clear() {
        initialized = true;
        target = null;
        getter = null;
    }

    public T get() {
        if (!initialized) {
            try {
                target = getter.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            initialized = true;
        }
        return target;
    }

}
