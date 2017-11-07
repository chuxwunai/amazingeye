package eye.restul.server;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;


public class PathVariableExtractor extends AbstractVariableExtractor {
    
    /** 配合SPI的无参构造函数 */
    public PathVariableExtractor() {
        super(null, null);
    }
	
    public PathVariableExtractor(String variableName, Type variableType) {
        super(variableName, variableType);
    }

    public Object extract(ResourceInvocation invocation) throws Exception {
	    Map<String,String> pathVariables = invocation.getPathVariables();
	    if (pathVariables.containsKey(variableName)) {
	        if (variableType instanceof Class && ((Class<?>) variableType).isArray() || variableType instanceof GenericArrayType) {
	            String[] values = (String[]) ConvertUtils.convertFromString(decode(pathVariables.get(variableName)), String[].class);
	            return ConvertUtils.convertFromStringArray(values, variableType);
	        } else {
	            return ConvertUtils.convertFromString(decode(pathVariables.get(variableName)), variableType);
	        }
        } else {
            // 变量名在路径变量中不存在
            // 判断是否使用了带"."的变量名来定义对象，如: a/{a.b}/{a.c}
            String prefix = variableName + ".";
            Map<String,String> properties = new HashMap<String,String>();
            for (Map.Entry<String,String> entry : pathVariables.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(prefix)) {
                    properties.put(key.substring(prefix.length()), entry.getValue());
                }
            }
            
            // 不存在以"{变量名}."开头的路径，则认为该变量的对象值为null
            if (properties.isEmpty()) return null;
            
            // FIXME
            Object bean = ((Class<?>) variableType).newInstance();
            BeanUtils.populate(bean, properties);
            return bean;
        }
    }
	
	public Object extract(Map<String,String> pathVariableMap, String content) throws Exception {
		if (pathVariableMap.containsKey(variableName)) {
		    return ConvertUtils.convertFromString(pathVariableMap.get(variableName), variableType);
		} else {
			String prefix = variableName + ".";
			Map<String,String> properties = new HashMap<String,String>();
			for (Map.Entry<String,String> entry : pathVariableMap.entrySet()) {
				String key = entry.getKey();
				if (key.startsWith(prefix)) {
					properties.put(key.substring(prefix.length()), entry.getValue());
				}
			}
			if (properties.isEmpty()) return null;
			
			// FIXME
            Object bean = ((Class<?>) variableType).newInstance();
			BeanUtils.populate(bean, properties);
			return bean;
		}
	}
	
	private static String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            return s;
        }
    }

}
