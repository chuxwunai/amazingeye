package eye.restul.server;

import java.lang.reflect.Type;



public class DefaultVariableExtractor extends AbstractVariableExtractor {
	
	private PathVariableExtractor pathVariableExtractor;
	
	private QueryVariableExtractor queryVariableExtractor;
	
    public DefaultVariableExtractor(String variableName, Type variableType) {
		super(variableName, variableType);
		this.pathVariableExtractor = new PathVariableExtractor(variableName, variableType);
		this.queryVariableExtractor = new QueryVariableExtractor(variableName, variableType);
	}

	public Object extract(ResourceInvocation invocation) throws Exception {
	    Object result;
	    
	    // 1. 从路径变量中获取
	    result = pathVariableExtractor.extract(invocation);
	    if (result != null) return result;
	    
	    // 2. 从查询变量中获取
	    result = queryVariableExtractor.extract(invocation);
        if (result != null) return result;
        
        return result;
	}

}
