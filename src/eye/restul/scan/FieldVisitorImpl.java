package eye.restul.scan;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

class FieldVisitorImpl implements FieldVisitor {
    
    private List<String> annotations;

    private ClassVisitorImpl classVisitor;
    
    FieldVisitorImpl(ClassVisitorImpl classVisitor) {
        this.classVisitor = classVisitor;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (visible) {
            // 将 annotation 签名转换成类名
            String clazz = desc.substring(1, desc.length() - 1).replace('/', '.');
            if (annotations == null) {
                annotations = new ArrayList<String>();
            }
            annotations.add(clazz);
        }
        return new EmptyVisitor();
    }

    public void visitAttribute(Attribute attr) {}

    public void visitEnd() {
        if (annotations != null) {
            classVisitor.addAnnotations(annotations);
        }
    }

}
