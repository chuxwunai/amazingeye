package eye.restul.scan;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;


/**
 * 路由。用于匹配一个HTTP请求到资源类方法。路由描述了请求路径，请求方法，资源类，资源实例工厂，资源方法以及参数列表等信息。
 */
public class Route {
	
	private static final Logger logger = Logger.getLogger(Route.class.getName());
	private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("^\\{([\\w\\.]+)(:.*)?\\}$");
	
	private static final Pattern PATTERN_ANY = Pattern.compile(".*");
	
	/**
	 * 资源方法的参数列表
	 */
	private List<Parameter> parameters = new ArrayList<Parameter>();

	/**
	 * 请求方法定义
	 */
	private String requestMethod;
	
	/**
	 * 请求路径定义
	 */
	private String requestPath;

	/**
	 * 资源类
	 */
	private Class<?> resourceClass;
	
	/**
	 * 资源实例工厂
	 */
	private ResourceFactory resourceFactory;
	
	/**
	 * 资源方法
	 */
	private Method resourceMethod;
	
	/**
	 * 请求路径的分段
	 */
	private Segment[] segments;
	
	public Route(String requestPath, String requestMethod, Class<?> resourceClass, Method resourceMethod, ResourceFactory resourceFactory) {
		this.requestPath = (requestPath.startsWith("/") ? requestPath.substring(1) : requestPath);
		this.requestMethod = requestMethod;
		this.resourceClass = resourceClass;
		this.resourceMethod = resourceMethod;
		this.resourceFactory = resourceFactory;
		initSegments();
		initParameters();
	}
	
	/**
	 * 获取资源方法的参数列表
	 * 
	 * @return 参数列表
	 */
	public List<Parameter> getParameters() {
        return parameters;
    }
	
	/**
	 * 获取定义的请求方法
	 * 
	 * @return 请求方法，如 "GET", "POST"...
	 */
	public String getRequestMethod() {
        return requestMethod;
    }
	
	/**
     * 获取定义的请求路径
     * 
     * @return 请求路径，如 "user/{id}"
     */
	public String getRequestPath() {
        return requestPath;
    }
	
	/**
	 * 获取资源类
	 * 
	 * @return 类
	 */
	public Class<?> getResourceClass() {
        return resourceClass;
    }
	
    /**
	 * 获取资源实例工厂
	 * 
	 * @return 资源实例工厂
	 */
	public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    /**
     * 获取资源方法
     * 
     * @return 方法
     */
    public Method getResourceMethod() {
        return resourceMethod;
    }

    /**
     * 获取资源分段
     * 
     * @return 分段数组
     */
    public Segment[] getSegments() {
        return segments;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("接口：").append(requestMethod).append(' ').append(requestPath).append("\n>>> ");
        builder.append(abbrName(resourceMethod.getReturnType())).append(' ')
               .append(resourceMethod.getDeclaringClass().getName()).append('.').append(resourceMethod.getName()).append('(');
        
        Class<?>[] types = resourceMethod.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) builder.append(',');
            builder.append(abbrName(types[i]));
        }
        builder.append(')');
        
        return builder.toString();
	}
    
    /**
	 * 初始化参数
	 */
	private void initParameters() {
		Type[] parameterTypes = resourceMethod.getGenericParameterTypes();
		int length = parameterTypes.length;
		
		// 通过解析 class 文件来获取方法的参数名，无法用反射取到
		List<LocalVariable> localVariables = new ArrayList<LocalVariable>();
		try {
			// 有些方法声明在父类中，因此需要从 declaringClass 中查找
			Class<?> declaringClass = resourceMethod.getDeclaringClass();
			ClassParser parser = new ClassParser(
				this.getClass().getResourceAsStream("/" + declaringClass.getName().replaceAll("\\.", "/") + ".class"), 
				declaringClass.getSimpleName() + ".class"
			);
			JavaClass clazz = parser.parse();
			org.aspectj.apache.bcel.classfile.Method method = clazz.getMethod(resourceMethod);
			org.aspectj.apache.bcel.classfile.LocalVariableTable localVariableTable = method.getLocalVariableTable();
			
			if (localVariableTable == null) {
				logger.info("无法从 class 文件中读取方法的参数名称，用参数 -g 或 -g:vars 来编译以避免此问题: " + resourceMethod);
				return;
			}
			
			localVariables.addAll(Arrays.asList(localVariableTable.getLocalVariableTable()));
			Collections.sort(localVariables, new Comparator<LocalVariable>() {
				public int compare(LocalVariable v0, LocalVariable v1) {
					return v0.getIndex() - v1.getIndex();
				}
			});
		} catch (IOException e) {
			logger.info(e.getLocalizedMessage()+ e);
		}
		
		for (int i = 0; i < length; i++) {
		    String variableName = localVariables.get(i + 1).getName();
		    parameters.add(new Parameter(variableName, parameterTypes[i]));
		}
	}

    private void initSegments() {
        List<String> segmentSources = convertToSegmentValues(requestMethod, requestPath);
	    segments = new Segment[segmentSources.size()];
	    for (int i = 0; i < segments.length; i++) {
	        String source = segmentSources.get(i);
	        Segment segment = new Segment();
	        segment.setSource(source);
	        
	        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(source);
            if (matcher.matches()) {
                segment.setVariableName(matcher.group(1));
                if (matcher.group(2) != null) {
                    segment.setPattern(Pattern.compile(matcher.group(2).substring(1)));
                } else {
                    segment.setPattern(PATTERN_ANY);
                }
            }
            
            segments[i] = segment;
	    }
	}
    
    /**
     * 将请求路径根据'/'分隔符进行分段。例如：<br>
     * GET /aaa/bbb/ccc => ["GET", "aaa", "bbb", "ccc"]<br>
     * <br>
     * 对于请求路径以 '/' 结尾的，分段最后补上空字符串，如：<br>
     * GET /aaa/bbb/ccc/ => ["GET", "aaa", "bbb", "ccc", ""]<br>
     *
     * @param method 请求方法，如 "GET", "POST"...
     * @param path 请求路径，如 "aaa/bbb/ccc"。请求路径开头的 "/" 会被忽略
     * @return 分段列表
     */
    public static List<String> convertToSegmentValues(String method, String path) {
        List<String> values = new ArrayList<String>();
        values.add(method);
        
        int from = 0, to = 0;
        while (from < path.length()) {
            to = path.indexOf('/', from);
            
            if (to == -1) {
                values.add(path.substring(from));
                break;
            } else {
                if (to != 0) {
                    values.add(path.substring(from, to));
                }
                from = to + 1;
            }
        }
        
        if (to != -1) {
            values.add("");
        }
        
        return values;
    }

    private static final String abbrName(Class<?> type) {
        String name = type.getCanonicalName();
        if (name.startsWith("java.lang.") || name.startsWith("java.util.")) {
            return name.substring(10);
        } else {
            return name;
        }
    }
	
}
