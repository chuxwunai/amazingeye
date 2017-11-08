package eye.restul.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ServletContext {

	private static final ThreadLocal<Map<Class<?>, Object>> contexts = new ThreadLocal<Map<Class<?>, Object>>();

	public static <T> void bind(Class<T> clazz, T instance) {
		Map<Class<?>, Object> context = contexts.get();
		if (context == null) {
			context = new HashMap<Class<?>, Object>();
			contexts.set(context);
		}
		context.put(clazz, instance);
	}

	public static void bind(final HttpServletRequest request, final HttpServletResponse response) {
		bind(HttpServletRequest.class, request);
		bind(HttpServletResponse.class, response);
		bind(HttpSession.class, LazyObject.create(HttpSession.class, new Callable<HttpSession>() {
			public HttpSession call() throws Exception {
				return request.getSession();
			}
		}));
	}

	public static boolean containsValue(Class<?> clazz) {
		Map<Class<?>, Object> context = contexts.get();
		if (context == null)
			return false;
		return context.containsKey(clazz);
	}

	public static HttpServletRequest currentRequest() {
		return currentValue(HttpServletRequest.class);
	}

	public static HttpServletResponse currentResponse() {
		return currentValue(HttpServletResponse.class);
	}

	public static HttpSession currentSession() {
		return currentValue(HttpSession.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T currentValue(Class<?> clazz) {
		Map<Class<?>, Object> context = contexts.get();
		if (context == null)
			return null;
		return (T) context.get(clazz);
	}

	public static void unbind() {
		unbind(HttpServletRequest.class, HttpServletResponse.class, HttpSession.class);
	}

	public static void unbind(Class<?>... classes) {
		Map<Class<?>, Object> context = contexts.get();
		if (context != null) {
			for (Class<?> clazz : classes) {
				context.remove(clazz);
			}
			if (context.isEmpty()) {
				contexts.remove();
			}
		}
	}

}
