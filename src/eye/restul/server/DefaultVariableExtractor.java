package eye.restul.server;

import java.lang.reflect.Type;

public class DefaultVariableExtractor extends AbstractVariableExtractor {

	private ContentVariableExtractor contentExtractor;

	private PathVariableExtractor pathVariableExtractor;

	private QueryVariableExtractor queryVariableExtractor;
	
	private ServletContextVariableExtractor servletContextVariableExtractor;

	public DefaultVariableExtractor(String variableName, Type variableType) {
		super(variableName, variableType);
		this.contentExtractor = new ContentVariableExtractor(variableName, variableType);
		this.pathVariableExtractor = new PathVariableExtractor(variableName, variableType);
		this.queryVariableExtractor = new QueryVariableExtractor(variableName, variableType);
		this.servletContextVariableExtractor = new ServletContextVariableExtractor(variableName, variableType);
	}

	public Object extract(ResourceInvocation invocation) throws Exception {
		Object result;

		// 1. 从路径变量中获取
		result = pathVariableExtractor.extract(invocation);
		if (result != null)
			return result;

		// 2. 从查询变量中获取
		result = queryVariableExtractor.extract(invocation);
		if (result != null)
			return result;
		
		// 3. 从ServletContext中获取
        result = servletContextVariableExtractor.extract(invocation);
        if (result != null) return result;

		// 4. 从正文中获取
		result = contentExtractor.extract(invocation);

		return result;
	}

}
