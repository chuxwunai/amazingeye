package eye.restul.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.objectweb.asm.ClassReader;
/**
 *浏览指定目录下所有的class文件（包括jar包的class文件）
 */
public class ClassPathScanner {
	
	private static final String CLASS_FILE_EXTENSION = ".class";
	/** <注解全路径,多个clazz> */
	private MultiMap data = new MultiValueMap();
	
	/**
	 * 浏览实现，写入MultiValueMap
	 * @param rootPaths new String[]{"com/aa","com/bb"}
	 * @return
	 * @throws Exception
	 */
	public MultiMap scan(String[] rootPaths) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String path : rootPaths) {
    		for (Enumeration<URL> e = classLoader.getResources(path); e.hasMoreElements(); ) {
    			URL url = e.nextElement();
    			if ("jar".equals(url.getProtocol())) {
    				JarURLConnection conn = (JarURLConnection) url.openConnection();
    				handleArchive(path, conn.getJarFile());
    			} else {
    				File directory = new File(URLDecoder.decode(url.getFile(), "UTF-8")); // 注意：URL.getFile()返回值是未解码的
    				if (directory.isDirectory()) {
    					for (File file : directory.listFiles()) {
    						handleFile(path, file);
    					}
    				}
    			}
    		}
		}
		
		return data;
	}
	
	private void check(String path, InputStream in) throws IOException {
	    try {
    		String clazz = path.replace('/', '.');
    		ClassVisitorImpl visitor = new ClassVisitorImpl();
    		ClassReader reader = new ClassReader(in);
    		reader.accept(visitor, ClassReader.SKIP_CODE);
    		if (visitor.getAnnotations() != null) {
        		for (String annotation : visitor.getAnnotations()) {
        			data.put(annotation, clazz);
        		}
    		}
	    } finally {
	        in.close();
	    }
	}
	
	private void handleArchive(String path, JarFile jarFile) throws IOException {
		for (Enumeration<JarEntry> t = jarFile.entries(); t.hasMoreElements(); ) {
			JarEntry je = t.nextElement();
			String name = je.getName();
			if (name.startsWith(path + '/') && name.endsWith(CLASS_FILE_EXTENSION)) {
				check(name.substring(0, name.length() - CLASS_FILE_EXTENSION.length()), jarFile.getInputStream(je));
			}
		}
	}
	
	private void handleFile(String path, File file) throws IOException {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				handleFile(path + '/' + file.getName(), f);
			}
		} else {
			String name = file.getName();
			if (name.endsWith(CLASS_FILE_EXTENSION)) {
				check(path + '/' + name.substring(0, name.length() - CLASS_FILE_EXTENSION.length()), new FileInputStream(file));
			}
		}
	}

}
