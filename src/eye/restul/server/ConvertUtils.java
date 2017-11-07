package eye.restul.server;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtilsBean;

public class ConvertUtils {
    
    private static final ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
    
    
    public static Object convertFromString(String value, Type type) {
        if (type instanceof Class) {
            return convertUtilsBean.convert(value, (Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType == List.class) {
                // TODO
            } else if (rawType == Map.class) {
                // TODO
            } else {
                return convertUtilsBean.convert(value, (Class<?>) rawType);
            }
        }
        
        throw new UnsupportedOperationException();
    }
    
    public static Object convertFromStringArray(String[] values, Type type) {
        Object array = null;
        Type componentType;
        if (type instanceof Class) {
            Class<?> _type = (Class<?>) type;
            if (!_type.isArray()) {
                throw new IllegalArgumentException("类型不是数组：" + type);
            }
            array = Array.newInstance(_type.getComponentType(), values.length);
            componentType = _type.getComponentType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType _type = (GenericArrayType) type;
            componentType = _type.getGenericComponentType();
            if (componentType instanceof Class) {
            	array = Array.newInstance((Class<?>) componentType, values.length);
            } else if (componentType instanceof ParameterizedType) {
                array = Array.newInstance((Class<?>) ((ParameterizedType) componentType).getRawType(), values.length);
            } else if (componentType instanceof TypeVariable) {
                // TODO
                throw new UnsupportedOperationException();
            } else if (componentType instanceof WildcardType) {
                // TODO
                throw new UnsupportedOperationException();
            } else {
                throw new InternalError();
            }
        } else {
            // TODO
            throw new UnsupportedOperationException();
        }
        
        for (int i = 0; i < values.length; i++) {
            Array.set(array, i, convertFromString(values[i], componentType));
        }
        return array;
    }

}
