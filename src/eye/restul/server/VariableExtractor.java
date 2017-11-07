package eye.restul.server;

import java.lang.reflect.Type;


/**
 * 变量解析器接口。变量解析器负责根据变量名和变量类型，从资源请求中解析出变量的值。<br>
 * <br>
 * 单个变量解析器实例只会在一次请求中，解析单个变量的值。框架会根据路由中变量的定义，决定使用哪个实现来构造变量解析器实例。<br>
 * <br>
 * 该接口的不同实现会根据资源调用对象中的不同属性（如路径，查询字符串，正文等）作解析，
 * 这些属性最终也是通过对HTTP请求做解析得到的
 * 
 * @author gmice
 */
public interface VariableExtractor {
	
	/**
	 * 根据传入的资源调用对象实例，解析变量的值
	 * 
	 * @param invocation 资源调用对象
	 * @return 解析得到的变量的值
	 * @throws Exception
	 */
	Object extract(ResourceInvocation invocation) throws Exception;
	
	Class<?> getAnnotationType();
	
	/**
	 * 获取变量名
	 * 
	 * @return 变量名
	 */
	String getVariableName();
	
	/**
	 * 获取变量类型
	 * 
	 * @return 变量类型
	 */
	Type getVariableType();

}
