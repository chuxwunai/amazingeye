package eye.restul.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

public class LazyObject {
	
	@SuppressWarnings("unchecked")
	public static <T> T create(final Class<T> clazz, final Callable<T> constructor) {
		if (clazz.isInterface()) {
			return (T) Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(), 
				new Class<?>[] { clazz }, 
				new InvocationHandler() {
					private boolean initialized = false;
					private T target;
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if (!initialized) {
							try {
								target = constructor.call();
							} finally {
								initialized = true;
							}
						}
						return method.invoke(target, args);
					}
				}
			);
		}
		
		throw new UnsupportedOperationException();
	}

}
