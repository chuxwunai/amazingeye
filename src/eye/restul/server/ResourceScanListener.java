package eye.restul.server;

import java.util.List;

import eye.restul.server.annotation.ClassPathScanFor;
import eye.restul.server.annotation.Resource;

@ClassPathScanFor(Resource.class)
public class ResourceScanListener implements ClassPathScanListener {

	public static List<String> classes;

	public void listen(List<String> classes) {
		ResourceScanListener.classes = classes;
	}

}
