package eye.restul;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.collections.MultiMap;

import eye.restul.annotation.ClassPathScanFor;
import eye.restul.scan.ClassPathScanListener;
import eye.restul.scan.ClassPathScanner;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValueVisitor;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

public class RestfulListener implements ServletContextListener {

	private static Logger logger = Logger.getLogger(RestfulListener.class.getName());

	private MultiMap classPathScanResult;

	private final String classPathScanForClassName = ClassPathScanFor.class.getName();

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		/** 1. 类扫描 */
		logger.info("开始类扫描");
		long startAt = System.currentTimeMillis();
		try {
			classPathScanResult = new ClassPathScanner().scan(new String[] { "com/sihuatech", "com/onewaveinc" });
		} catch (Exception e) {
			logger.log(Level.SEVERE, "类扫描出错", e);
			return;
		}
		logger.info("结束类扫描，耗时[" + (System.currentTimeMillis() - startAt) + "]毫秒");

		/** 4. 处理其他类扫描监听器 */
		Set<String> listeners = new HashSet<String>(
				(Collection<String>) classPathScanResult.get(classPathScanForClassName));
		for (String listener : listeners) {
			processListener(listener);
		}

		// 类扫描结果不再需要，释放掉
		classPathScanResult = null;

	}

	private void processListener(String listener) {
		try {
			String classPathScanFor = null;

			ClassFile clazz = new ClassFile(false, listener, null);
			AnnotationsAttribute annotations = (AnnotationsAttribute) clazz
					.getAttribute(AnnotationsAttribute.visibleTag);
			for (Annotation annotation : annotations.getAnnotations()) {
				if (classPathScanForClassName.equals(annotation.getTypeName())) {
					AnnotationMemberValueVisitor visitor = new AnnotationMemberValueVisitor();
					annotation.getMemberValue("value").accept(visitor);
					classPathScanFor = visitor.className;
					break;
				}
			}

			// 获取监听器类
			Class<?> listenerClass = Thread.currentThread().getContextClassLoader().loadClass(listener);

			// 构建监听器实例
			ClassPathScanListener listenerInstance = (ClassPathScanListener) listenerClass.newInstance();

			// 从扫描结果中获取该监听器需要的类的名称列表，调用监听接口
			System.out.println("**************classPathScanFor=" + classPathScanFor);
			Collection<String> classes = (Collection<String>) classPathScanResult.get(classPathScanFor);
			if (classes != null && !classes.isEmpty()) {
				System.out.println("**************classes=" + classes);
				listenerInstance.listen(new ArrayList<String>(new HashSet<String>(classes)));
			}
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "处理类扫描监听器时出错，监听器类: " + listener, e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	private static class AnnotationMemberValueVisitor implements MemberValueVisitor {

		String className;

		public void visitAnnotationMemberValue(AnnotationMemberValue arg0) {
		}

		public void visitArrayMemberValue(ArrayMemberValue arg0) {
		}

		public void visitBooleanMemberValue(BooleanMemberValue arg0) {
		}

		public void visitByteMemberValue(ByteMemberValue arg0) {
		}

		public void visitCharMemberValue(CharMemberValue arg0) {
		}

		public void visitClassMemberValue(ClassMemberValue value) {
			className = value.getValue();
		}

		public void visitDoubleMemberValue(DoubleMemberValue arg0) {
		}

		public void visitEnumMemberValue(EnumMemberValue arg0) {
		}

		public void visitFloatMemberValue(FloatMemberValue arg0) {
		}

		public void visitIntegerMemberValue(IntegerMemberValue arg0) {
		}

		public void visitLongMemberValue(LongMemberValue arg0) {
		}

		public void visitShortMemberValue(ShortMemberValue arg0) {
		}

		public void visitStringMemberValue(StringMemberValue arg0) {
		}

	}

}
