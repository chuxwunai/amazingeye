package eye.restul.server;

//import org.springframework.context.ApplicationContext;


public class SpringResourceFactory implements ResourceFactory {
	
    private Class<?> clazz;
    
    private String name;
	
	public SpringResourceFactory(String name, Class<?> clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public Object create() throws Exception {
//		ApplicationContext applicationContext = null;
//		return applicationContext.getBean(name, clazz);
		return null;
	}
	
	public String toString() {
		return "spring:" + name;
	}

}
