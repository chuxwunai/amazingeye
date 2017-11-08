package eye.demo;

import java.util.ArrayList;
import java.util.List;

import eye.restul.server.annotation.Delete;
import eye.restul.server.annotation.Get;
import eye.restul.server.annotation.Path;
import eye.restul.server.annotation.Post;
import eye.restul.server.annotation.Put;
import eye.restul.server.annotation.Resource;



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

	@Post
	@Path("service")
	public List<Service> post(Service serivceDemo){
		List<Service> list = Service.getServices();
		for (int i = 0; i < list.size(); i++) {
			Service persistent = list.get(i);
			if (serivceDemo.getName().equals(persistent.getName())) {
				list.remove(i);
				break;
			}
		}
		return list;
	}
	
	@Put
	@Path("service")
	public List<Service> put(Service serivceDemo){
		List<Service> list = Service.getServices();
		for (int i = 0; i < list.size(); i++) {
			Service persistent = list.get(i);
			if (serivceDemo.getName().equals(persistent.getName())) {
				list.remove(i);
				break;
			}
		}
		return list;
	}
	
	@Delete
	@Path("service")
	public List<Service> del(Service serivceDemo){
		List<Service> list = Service.getServices();
		for (int i = 0; i < list.size(); i++) {
			Service persistent = list.get(i);
			if (serivceDemo.getName().equals(persistent.getName())) {
				list.remove(i);
				break;
			}
		}
		return list;
	}
}
