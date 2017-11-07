package eye.restul.server;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

class MethodVisitorImpl implements MethodVisitor {
    
    private List<String> annotations;
    
    private ClassVisitorImpl classVisitor;
    
    MethodVisitorImpl(ClassVisitorImpl classVisitor) {
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
        return null;
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return null;
    }

    public void visitAttribute(Attribute attr) {}

    public void visitCode() {}

    public void visitEnd() {
        if (annotations != null) {
            classVisitor.addAnnotations(annotations);
        }
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {}

    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {}

    public void visitIincInsn(int var, int increment) {}

    public void visitInsn(int opcode) {}

    public void visitIntInsn(int opcode, int operand) {}

    public void visitJumpInsn(int opcode, Label label) {}

    public void visitLabel(Label label) {}

    public void visitLdcInsn(Object cst) {}

    public void visitLineNumber(int line, Label start) {}

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {}

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {}

    public void visitMaxs(int maxStack, int maxLocals) {}

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {}

    public void visitMultiANewArrayInsn(String desc, int dims) {}

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        return null;
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {}

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {}

    public void visitTypeInsn(int opcode, String type) {}

    public void visitVarInsn(int opcode, int var) {}

}
