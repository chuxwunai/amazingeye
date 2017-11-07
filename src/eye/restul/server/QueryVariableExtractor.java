package eye.restul.server;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

public class QueryVariableExtractor extends AbstractVariableExtractor {

	/** 配合SPI的无参构造函数 */
	public QueryVariableExtractor() {
		super(null, null);
	}

	public QueryVariableExtractor(String variableName, Type variableType) {
		super(variableName, variableType);
	}

	public Object extract(ResourceInvocation invocation) throws Exception {
		if (variableName.length() == 0) {
			// 将整个查询字符串作为参数值
			String queryString = invocation.getQueryString();
			queryString = queryString.replaceFirst("(^|&)_=\\d+$", "");
			return ConvertUtils.convertFromString(decode(queryString), variableType);
		} else {
			return extract(invocation.getQueryVariables());
		}
	}

	public Object extract(Map<String, String[]> queryVariables) throws Exception {
		if (queryVariables.containsKey(variableName)) {
			// 变量名在查询字符串中存在，即URL中"?"后存在"{变量名}={变量值}"
			if ((variableType instanceof Class && ((Class<?>) variableType).isArray())
					|| variableType instanceof GenericArrayType) {
				// 变量类型是数组。例:
				// ?a=1&a=2&a=3 => a=[1,2,3]
				// ?a=1 => a=[1]
				return ConvertUtils.convertFromStringArray(decode(queryVariables.get(variableName)), variableType);
			} else {
				// 变量类型不是数组。例:
				// ?a=1 => a=1
				return ConvertUtils.convertFromString(decode(queryVariables.get(variableName)[0]), variableType);
			}
		} else {
			// 变量名在查询字符串中不存在
			// 判断是否使用了带"."的变量名来定义对象，如: ?a.b=1&a.c=1
			String prefix = variableName + ".";
			Map<String, String[]> properties = new HashMap<String, String[]>();
			for (Map.Entry<String, String[]> entry : queryVariables.entrySet()) {
				String key = entry.getKey();
				if (key.startsWith(prefix)) {
					properties.put(key.substring(prefix.length()), decode(entry.getValue()));
				}
			}

			// 不存在以"{变量名}."开头的查询参数，则认为该变量的对象值为null
			if (properties.isEmpty())
				return null;

			// 转换成对象
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

	private static String[] decode(String[] s) {
		try {
			String[] t = new String[s.length];
			for (int i = 0; i < s.length; i++) {
				t[i] = URLDecoder.decode(s[i], "UTF-8");
			}
			return t;
		} catch (UnsupportedEncodingException ignore) {
			return s;
		}
	}

}
