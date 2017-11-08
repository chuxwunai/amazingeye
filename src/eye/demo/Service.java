package eye.demo;

import java.util.ArrayList;
import java.util.List;

public class Service {

	private int num;
	private String name;

	public Service() {
		super();
	}

	public Service(int num, String name) {
		super();
		this.num = num;
		this.name = name;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static List<Service> getServices(){
		List<Service> list = new ArrayList<Service>(10);
		for(int i = 1; i<=10;i++){
			list.add(new Service(i, "name"+i));
		}
		return list;
	}
}
