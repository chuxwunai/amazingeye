package eye.restul.server;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 正文变量解析器。负责从HTTP请求的正文中解析值
 * 
 * @author gmice
 */
public class ContentVariableExtractor extends AbstractVariableExtractor {
    
    /**
     * 转换器接口
     */
    private static interface Converter {
        Object convert(String content);
    }
    
    private static class BooleanConverter implements Converter {
        public Object convert(String content) {
            return content == null ? null : Boolean.parseBoolean(content);
        }
    }
    
    private static class ByteConverter implements Converter {
        public Object convert(String content) {
            return content == null ? null : Byte.parseByte(content);
        }
    }
    
//    private static class CharacterConverter implements Converter {
//        public Object convert(String content) {
//            return content == null ? null : JSONArray.fromObject("[" + content + "]", JsonConfigHolder.jsonConfig).getString(0).charAt(0);
//        }
//    }
    
    private static class DoubleConverter implements Converter {
        public Object convert(String content) {
            return content == null ? null : Double.parseDouble(content);
        }
    }
    
    private static class FloatConverter implements Converter {
        public Object convert(String content) {
            return content == null ? null : Float.parseFloat(content);
        }
    }
    
    private static class IntegerConverter implements Converter {
        public Object convert(String content) {
            return content == null ? null : Integer.parseInt(content);
        }
    }
    
    private static class LongConverter implements Converter {
        public Object convert(String content) {
            return content == null ? null : Long.parseLong(content);
        }
    }
    
    private static class ShortConverter implements Converter {
        public Object convert(String content) {
            return content == null ? null : Short.parseShort(content);
        }
    }
    
//    private static class StringConverter implements Converter {
//        public Object convert(String content) {
//            return (content == null || content.length() == 0) ? null : JSONArray.fromObject("[" + content + "]", JsonConfigHolder.jsonConfig).getString(0);
//        }
//    }
    
    /**
     * 特殊转换器表，用于 字符串->基本类型 的转换
     */
    private static final Map<Class<?>,Converter> SPECIAL_CONVERTERS = new HashMap<Class<?>,Converter>();
    
    static {
        // 注册特殊转换器
        SPECIAL_CONVERTERS.put(Boolean.TYPE, new BooleanConverter());
        SPECIAL_CONVERTERS.put(Boolean.class, new BooleanConverter());
        SPECIAL_CONVERTERS.put(Byte.TYPE, new ByteConverter());
        SPECIAL_CONVERTERS.put(Byte.class, new ByteConverter());
//        SPECIAL_CONVERTERS.put(Character.TYPE, new CharacterConverter());
//        SPECIAL_CONVERTERS.put(Character.class, new CharacterConverter());
        SPECIAL_CONVERTERS.put(Double.TYPE, new DoubleConverter());
        SPECIAL_CONVERTERS.put(Double.class, new DoubleConverter());
        SPECIAL_CONVERTERS.put(Float.TYPE, new FloatConverter());
        SPECIAL_CONVERTERS.put(Float.class, new FloatConverter());
        SPECIAL_CONVERTERS.put(Integer.TYPE, new IntegerConverter());
        SPECIAL_CONVERTERS.put(Integer.class, new IntegerConverter());
        SPECIAL_CONVERTERS.put(Long.TYPE, new LongConverter());
        SPECIAL_CONVERTERS.put(Long.class, new LongConverter());
        SPECIAL_CONVERTERS.put(Short.TYPE, new ShortConverter());
        SPECIAL_CONVERTERS.put(Short.class, new ShortConverter());
//        SPECIAL_CONVERTERS.put(String.class, new StringConverter());
    }
    
    private ObjectMapper objectMapper;
    
    private JavaType variableJavaType;
    
    /** 配合SPI的无参构造函数 */
    public ContentVariableExtractor() {
        super(null, null);
    }
    
    /**
     * @param variableName 变量名
     * @param variableType 变量类型
     */
    public ContentVariableExtractor(String variableName, Type variableType) {
        super(variableName, variableType);
        objectMapper = ObjectMapperHolder.getObjectMapper();
        variableJavaType = objectMapper.constructType(variableType);
    }
    
	/* (non-Javadoc)
	 * @see com.onewaveinc.bumblebee.core.web.extractor.VariableExtractor#extract(com.onewaveinc.bumblebee.core.web.ResourceInvocation)
	 */
	public Object extract(ResourceInvocation invocation) throws Exception {
	    String content = invocation.getContent();
	    
	    // 获取变量类型对应的特殊转换器
        Converter converter = SPECIAL_CONVERTERS.get(variableType);

        if (converter != null) {
            // 存在特殊转换器，说明变量是基本类型，直接用转换器进行转换
            return converter.convert(content);
        } else {
            // 不存在特殊转换器，说明变量是对象类型
            return content == null ? null : objectMapper.readValue(content, variableJavaType);
        }
	}

}
