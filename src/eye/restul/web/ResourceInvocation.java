package eye.restul.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ClassUtils;

import eye.restul.scan.ObjectReference;
import eye.restul.scan.QueryVariableMap;
import eye.restul.scan.Route;
import eye.restul.scan.Segment;
import eye.restul.scan.StrongReference;

public class ResourceInvocation {

	private static final Set<String> IGNORE_CONTENT_METHODS = new HashSet<String>(Arrays.asList("GET", "UPLOAD"));

	private static final Logger logger = Logger.getLogger(ResourceInvocation.class.getName());

	private String content;
	/**
	 * 请求方法
	 */
	private String method;

	private ObjectReference<Object>[] parameters;

	/**
	 * 请求路径
	 */
	private String path;

	private Map<String, String> pathVariables;


	/**
	 * 请求查询字符串
	 */
	private String queryString;

	private Map<String, String[]> queryVariables;

	private HttpServletRequest request;

	private HttpServletResponse response;

	private Route route;

	private int statusCode = HttpServletResponse.SC_OK;

	public ResourceInvocation(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		init();
	}

	public String error(int code) throws IOException {
		statusCode = code;
		response.setStatus(code);
		return null;
	}

	/**
	 * 以字符串形式获取请求正文。若请求方法是忽略正文的（如GET），则返回<code>null</code>
	 * 
	 * @return 请求正文
	 */
	public String getContent() {
		// 已经读取过请求正文，直接返回之前读取的结果
		if (content != null)
			return content;

		// 若请求方法是忽略正文的，返回null
		if (IGNORE_CONTENT_METHODS.contains(method))
			return null;

		// 若请求方法不是忽略正文时，从HttpServletRequest中读取请求正文
		try {
			InputStream in = request.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int c;
			while ((c = in.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, c);
			}

			content = out.toString("UTF-8");
		} catch (IOException e) {
			logger.info(e.getLocalizedMessage() + e);
			content = "";
		}

		return content;
	}

	/**
	 * 获取请求方法
	 * 
	 * @return 请求方法
	 */
	public String getMethod() {
		return method;
	}

	public Object[] getParameters() {
		if (parameters == null)
			return new Object[0];
		Object[] result = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			ObjectReference<Object> parameter = parameters[i];
			result[i] = (parameter == null ? null : parameter.get());
		}
		return result;
	}

	/**
	 * 获取请求的资源路径
	 * 
	 * @return 请求的资源路径
	 */
	public String getPath() {
		return path;
	}

	public Map<String, String> getPathVariables() {
		if (pathVariables != null)
			return pathVariables;
		if (route == null)
			return null;

		pathVariables = new HashMap<String, String>();

		List<String> segmentValues = Route.convertToSegmentValues(method, path);
		Segment[] segments = route.getSegments();
		for (int i = 0; i < segmentValues.size(); i++) {
			Segment segment = segments[i];
			if (segment.getPattern() == null)
				continue;
			Matcher matcher = segment.getPattern().matcher(segmentValues.get(i));
			if (matcher.matches()) {
				if (matcher.groupCount() == 1) {
					pathVariables.put(segment.getVariableName(), matcher.group(1));
				} else {
					pathVariables.put(segment.getVariableName(), matcher.group());
				}
			} else {
				throw new IllegalStateException("尝试从不匹配的路由中获取路径变量。路由{" + route + "}，路径{" + path + "}");
			}
		}

		return pathVariables;
	}

	/**
	 * 获取请求的查询字符串
	 * 
	 * @return 请求的查询字符串
	 */
	public String getQueryString() {
		return queryString;
	}

	public Map<String, String[]> getQueryVariables() {
		if (queryVariables == null) {
			queryVariables = QueryVariableMap.parseFrom(queryString);
		}
		return queryVariables;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * 获取路由
	 * 
	 * @return 路由
	 */
	public Route getRoute() {
		return route;
	}

	public HttpSession getSession() {
		return request.getSession();
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Object invoke() throws Throwable {
		try {
			Object resource = route.getResourceFactory().create();

			Object[] parameters = getParameters();
			int length = parameters.length;
			Method method = route.getResourceMethod();

			// 检查参数是否与方法签名吻合
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (length != parameterTypes.length) {
				throw new IllegalArgumentException(
						"变量个数不匹配。" + route + "，期望个数{" + parameterTypes.length + "}，实际个数{" + length + "}");
			}

			boolean invalid = false;
			for (int i = 0; i < length; i++) {
				Class<?> type = parameterTypes[i];
				if (type.isInstance(parameters[i]))
					continue;
				if (type.isPrimitive()) {
					type = ClassUtils.primitiveToWrapper(type);
					if (type.isInstance(parameters[i]))
						continue;
				} else {
					if (parameters[i] == null)
						continue;
				}
				invalid = true;
				break;
			}

			if (invalid) {
				StringBuilder builder = new StringBuilder();
				builder.append("变量类型不匹配。").append(route).append("，期望类型{[");
				for (int i = 0; i < length; i++) {
					if (i > 0)
						builder.append(',');
					builder.append(parameterTypes[i].getName());
				}
				builder.append("]}，实际类型{[");
				for (int i = 0; i < length; i++) {
					if (i > 0)
						builder.append(',');
					builder.append(parameters[i] == null ? "null" : parameters[i].getClass().getName());
				}
				builder.append("]}");
				throw new IllegalArgumentException(builder.toString());
			}

			return method.invoke(resource, parameters);
		} catch (InvocationTargetException e) {
			statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			throw e.getCause();
		}
	}

	public void setParameter(int index, Object value) {
		setParameter(index, new StrongReference<Object>(value));
	}

	public void setParameter(int index, ObjectReference<Object> value) {
		ensureCapacityOfParameters(index + 1);
		parameters[index] = value;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	@SuppressWarnings("unchecked")
	private void ensureCapacityOfParameters(int capacity) {
		if (this.parameters == null) {
			this.parameters = new ObjectReference[capacity];
			return;
		}

		if (this.parameters.length < capacity) {
			ObjectReference<Object>[] parameters = new ObjectReference[capacity];
			System.arraycopy(this.parameters, 0, parameters, 0, this.parameters.length);
			this.parameters = parameters;
		}
	}

	private void init() {
		// 获取请求方法
		method = (String) request.getAttribute("OW-Request");
		if (method == null) {
			method = request.getHeader("OW-Request");
			if (method == null) {
				method = request.getMethod();
			}
		}

		path = (String) request.getAttribute("javax.servlet.include.path_info");
		if (path == null) {
			// 获得请求路径，此处不使用 request.getPathInfo 以防止编码问题
			path = request.getRequestURI();
			path = path.substring(request.getContextPath().length() + request.getServletPath().length());
			try {
				path = URLDecoder.decode(path, "UTF-8");
			} catch (UnsupportedEncodingException ignore) {
			}
		}

		queryString = request.getQueryString();
	}

}
