package eye.demo;

import java.util.ArrayList;
import java.util.List;

import eye.restul.annotation.Get;
import eye.restul.annotation.Path;
import eye.restul.annotation.Resource;

@Resource("ext")
public class ServiceExt {
	
	@Get
	public void get(){
		System.out.println("ServiceExt get()");
	}
	
	@Get
	@Path("path")
	public void getByPath(){
		System.out.println("ServiceExt path getByPath()");
	}
	
	@Get
	@Path("path/{a}/{b}")
	public void getByPathParam(String a,Long b){
		System.out.println("ServiceExt path getByPathParam()");
		if(a instanceof String){
			System.out.println("param a is String");
		}
		if(b instanceof Long){
			System.out.println("param b is Long");
		}
	}
	
	@Get
	@Path("path/{a}/{b}")
	public List<String> getByPathParamReturn(String a,Long b){
		System.out.println("ServiceExt path getByPathParamReturn()");
		if(a instanceof String){
			System.out.println("param a is String");
		}
		if(b instanceof Long){
			System.out.println("param b is Long");
		}
		List<String> init = new ArrayList<String>();
		init.add(a);
		init.add(b.toString());
		return init;
	}
}