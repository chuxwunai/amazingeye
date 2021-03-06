package eye.restul.entrance;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.collections.MultiMap;

import eye.restul.server.ClassPathScanListener;
import eye.restul.server.ClassPathScanner;
import eye.restul.server.annotation.ClassPathScanFor;

/**
 * 扫描应用
 */
public class RestfulListener implements ServletContextListener {

	private static Logger logger = Logger.getLogger(RestfulListener.class.getName());

	private MultiMap classPathScanResult;

	/** eye.restul.annotation.ClassPathScanFor */
	private final String classPathScanForClassName = ClassPathScanFor.class.getName();

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		/** 1. 类扫描 */
		logger.info("开始类扫描");
		long startAt = System.currentTimeMillis();
		try {
			classPathScanResult = new ClassPathScanner().scan(new String[] { "eye" });
		} catch (Exception e) {
			logger.log(Level.SEVERE, "类扫描出错", e);
			return;
		}
		logger.info("结束类扫描，耗时[" + (System.currentTimeMillis() - startAt) + "]毫秒");

		/** 2. 处理类扫描监听器 */
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
			Class<?> clazz = Class.forName(listener);
			for (Annotation annotation : clazz.getAnnotations()) {
				if (classPathScanForClassName.equals(annotation.annotationType().getName())) {
					classPathScanFor = ((ClassPathScanFor) annotation).value().getName();
					break;
				}
			}
			// 构建监听器实例
			ClassPathScanListener listenerInstance = (ClassPathScanListener) clazz.newInstance();

			// 从扫描结果中获取该监听器需要的类的名称列表，调用监听接口
			Collection<String> classes = (Collection<String>) classPathScanResult.get(classPathScanFor);
			if (classes != null && !classes.isEmpty()) {
				listenerInstance.listen(new ArrayList<String>(new HashSet<String>(classes)));
			}
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "处理类扫描监听器时出错，监听器类: " + listener, e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

}
