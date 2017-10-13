package eye.restul.scan;

public class DefaultResourceFactory implements ResourceFactory {
	
	private Class<?> clazz;
	
	public DefaultResourceFactory(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Object create() throws Exception {
		return clazz.newInstance();
	}
	
	@Override
	public String toString() {
		return "class:" + clazz.getName();
	}

}
