package eye.restul.scan;

public class StrongReference<T> implements ObjectReference<T> {
    
    private T target;
    
    public StrongReference(T target) {
        this.target = target;
    }

    public void clear() {
        target = null;
    }

    public T get() {
        return target;
    }

}
