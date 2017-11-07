package eye.restul.server;

import java.lang.reflect.Type;

/**
 * 变量解析器基类。用来抽取出变量解析器类中通用的变量名和变量类型的处理部分
 * 
 * @author gmice
 */
public abstract class AbstractVariableExtractor implements VariableExtractor {
	
	/**
	 * 变量名
	 */
	protected String variableName;
	
	/**
	 * 变量类型
	 */
	protected Type variableType;
	
	/**
	 * @param variableName 变量名
	 * @param variableType 变量类型
	 */
	public AbstractVariableExtractor(String variableName, Type variableType) {
		this.variableName = variableName;
		this.variableType = variableType;
	}
	
	/* (non-Javadoc)
	 * @see com.onewaveinc.bumblebee.core.web.extractor.VariableExtractor#getAnnotationType()
	 */
	public Class<?> getAnnotationType() {
	    throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.onewaveinc.bumblebee.core.web.extractor.VariableExtractor#getVariableName()
	 */
	public String getVariableName() {
		return variableName;
	}
	
	/* (non-Javadoc)
	 * @see com.onewaveinc.bumblebee.core.web.extractor.VariableExtractor#getVariableType()
	 */
	public Type getVariableType() {
	    return variableType;
	}

}
