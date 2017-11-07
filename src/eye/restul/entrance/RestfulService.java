package eye.restul.entrance;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

import eye.restul.server.DefaultResourceFactory;
import eye.restul.server.ResourceFactory;
import eye.restul.server.ResourceInvocation;
import eye.restul.server.ResourceScanListener;
import eye.restul.server.Route;
import eye.restul.server.RouteRegistry;
import eye.restul.server.SpringResourceFactory;
import eye.restul.server.annotation.Bean;
import eye.restul.server.annotation.Delete;
import eye.restul.server.annotation.Get;
import eye.restul.server.annotation.Path;
import eye.restul.server.annotation.Post;
import eye.restul.server.annotation.Put;
import eye.restul.server.annotation.Resource;

/**
 * WEB入口
 */
@SuppressWarnings("serial")
public class RestfulService extends HttpServlet {

	private Logger logger = Logger.getLogger(RestfulService.class.getName());
	private static final byte[] NULL_BYTES = "null".getBytes();
	private boolean gzipEnabled;
	private int gzipThreshold;

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info("初始化资源...");
		if (ResourceScanListener.classes != null) {
			try {
				for (String className : ResourceScanListener.classes) {
					try {
						Class<?> clazz = Class.forName(className);
						initResource(clazz);
					} catch (ClassNotFoundException e) {
						logger.severe(e.getLocalizedMessage());
					}
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ResourceInvocation invocation = null;
		try {
			invocation = new ResourceInvocation(request, response);

			Object result = invocation.invoke();

			if (response.isCommitted())
				return;

			response.setContentType("application/json;charset=utf-8");

			byte[] buf = null;

			if (result == null) {
				buf = NULL_BYTES;
			} else {
				buf = JSON.toJSONString(result).getBytes();
			}
			OutputStream out = response.getOutputStream();

			if (gzipEnabled && gzipThreshold < buf.length) {
				String acceptEncoding = request.getHeader("Accept-Encoding");
				if (acceptEncoding != null && acceptEncoding.indexOf("gzip") >= 0) {
					response.setHeader("Content-Encoding", "gzip");
					out = new GZIPOutputStream(out);
				}
			}

			out.write(buf);
			out.close();
		} catch (Throwable e) {
			logger.severe(e.getMessage());
		}
	}

	/**
	 * 根据资源类中的标注，构建相关的路由
	 * 
	 * @param resourceClass
	 *            资源类
	 */
	private void initResource(Class<?> resourceClass) {
		Resource resource = resourceClass.getAnnotation(Resource.class);
		if (resource == null)
			return;

		ResourceFactory resourceFactory = null;

		Bean bean = resourceClass.getAnnotation(Bean.class);
		if (bean != null) {
			resourceFactory = new SpringResourceFactory(bean.value(), resourceClass);
		} else {
			resourceFactory = new DefaultResourceFactory(resourceClass);
		}

		logger.info("资源[" + resource.value() + "]，实例化方式[" + resourceFactory + "]");

		List<Route> routes = new ArrayList<Route>();
		for (Method method : resourceClass.getMethods()) {
			String resourcePath = resource.value();
			String methodPath = null;
			String requestMethod = null;
			for (Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType() == Path.class) {
					Path path = (Path) annotation;
					methodPath = path.value();
				}
				if (annotation.annotationType() == Get.class) {
					requestMethod = "GET";
				}
				if (annotation.annotationType() == Post.class) {
					requestMethod = "POST";
				}
				if (annotation.annotationType() == Put.class) {
					requestMethod = "PUT";
				}
				if (annotation.annotationType() == Delete.class) {
					requestMethod = "DELETE";
				}
				if (annotation.annotationType() == Resource.class) {
					resourcePath = ((Resource) annotation).value();
				}
			}

			if (requestMethod == null)
				continue;

			Route route = new Route(methodPath == null ? resourcePath : resourcePath + "/" + methodPath, requestMethod,
					resourceClass, method, resourceFactory);
			routes.add(route);
		}

		RouteRegistry.instance().addAll(routes);
	}
}
