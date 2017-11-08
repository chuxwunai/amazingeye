package eye.restul.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ObjectMapperHolder {

    private static final ObjectMapper instance = new ObjectMapper();
    
    private static class DateFormatProxy extends DateFormat {
        
        private static final long serialVersionUID = 1L;

        private SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        private SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        
        @Override
        public Object clone() {
            return new DateFormatProxy();
        }
        
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return df1.format(date, toAppendTo, fieldPosition);
        }

        @Override
        public Date parse(String source, ParsePosition pos) {
            Date date = df1.parse(source, pos);
            if (date == null) {
                date = df2.parse(source, pos);
            }
            return date;
        }
        
    }

    static {
        instance.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        instance.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        instance.setDateFormat(new DateFormatProxy());
        
        instance.setDefaultTyping(new DefaultTypeResolverBuilder(null) {
            private static final long serialVersionUID = 1L;

            public boolean useForType(JavaType t) {
                return t.getRawClass() == Object.class;
            }

            @Override
            public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
                TypeIdResolver idRes = idResolver(config, baseType, subtypes, true, false);
                return new AsPropertyTypeSerializer(idRes, null, _typeProperty) {
                    @Override
                    public void writeTypePrefixForArray(Object value, JsonGenerator jgen) throws IOException, JsonProcessingException {
                        jgen.writeStartArray();
                    }

                    @Override
                    public void writeTypePrefixForArray(Object value, JsonGenerator jgen, Class<?> type) throws IOException, JsonProcessingException {
                        jgen.writeStartArray();
                    }
                    
                    @Override
                    public void writeTypePrefixForObject(Object value, JsonGenerator jgen) throws IOException, JsonProcessingException {
                        if (value instanceof Map) {
                            jgen.writeStartObject();
                        } else {
                            super.writeTypePrefixForObject(value, jgen);
                        }
                    }
                    
                    @Override
                    public void writeTypePrefixForScalar(Object value, JsonGenerator jgen) throws IOException, JsonProcessingException {
                        // EMPTY
                    }

                    @Override
                    public void writeTypePrefixForScalar(Object value, JsonGenerator jgen, Class<?> type) throws IOException, JsonProcessingException {
                        // EMPTY
                    }

                    @Override
                    public void writeTypeSuffixForArray(Object value, JsonGenerator jgen) throws IOException, JsonProcessingException {
                        jgen.writeEndArray();
                    }

                    @Override
                    public void writeTypeSuffixForScalar(Object value, JsonGenerator jgen) throws IOException, JsonProcessingException {
                        // EMPTY
                    }
                };
            }
        }.init(JsonTypeInfo.Id.CLASS, null).inclusion(JsonTypeInfo.As.PROPERTY));

        SimpleModule module = new SimpleModule("ObjectMapperHolder", new Version(1, 0, 0, null));
        module.addKeyDeserializer(Class.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                try {
                    return Class.forName(key);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        instance.registerModule(module);
    }

    public static ObjectMapper getObjectMapper() {
        return instance;
    }
    
}
