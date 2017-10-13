package eye.restul.scan;

import java.util.List;

import eye.restul.annotation.ClassPathScanFor;
import eye.restul.annotation.Resource;


@ClassPathScanFor(Resource.class)
public class ResourceScanListener implements ClassPathScanListener {
	
	public static List<String> classes;

	public void listen(List<String> classes) {
		ResourceScanListener.classes = classes;
	}

}
