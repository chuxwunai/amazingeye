package eye.restul.scan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Order {
    
    static final String DEFAULT = "__default__"; 
    
    static final String OTHERS = "__others__";
    
    String[] groups() default { DEFAULT };

}
