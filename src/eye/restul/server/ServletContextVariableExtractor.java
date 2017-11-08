package eye.restul.server;

import java.lang.reflect.Type;


public class ServletContextVariableExtractor extends AbstractVariableExtractor {
    
    public ServletContextVariableExtractor(String variableName, Type variableType) {
        super(variableName, variableType);
    }
    
    public Object extract(ResourceInvocation invocation) throws Exception {
        if (variableType instanceof Class) {
            return ServletContext.currentValue((Class<?>) variableType);
        } else {
            return null;
        }
	}

}
