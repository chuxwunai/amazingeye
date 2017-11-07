package eye.restul.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

class ClassVisitorImpl implements ClassVisitor {
	
	private Set<String> annotations;

	public Set<String> getAnnotations() {
		return annotations;
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (visible) {
			// �? annotation 签名转换成类�?
			String clazz = desc.substring(1, desc.length() - 1).replace('/', '.');
			if (annotations == null) {
			    annotations = new HashSet<String>();
			}
			annotations.add(clazz);
		}
		return null;
	}
	
	public void visitAttribute(Attribute attr) {}

	public void visitEnd() {}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return new FieldVisitorImpl(this);
	}

    public void visitInnerClass(String name, String outerName, String innerName, int access) {}

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodVisitorImpl(this);
    }

    public void visitOuterClass(String owner, String name, String desc) {}

    public void visitSource(String source, String debug) {}

    void addAnnotations(Collection<String> annotations) {
	    if (this.annotations == null) {
	        this.annotations = new HashSet<String>(annotations);
	    } else {
	        this.annotations.addAll(annotations);
	    }
	}

}
