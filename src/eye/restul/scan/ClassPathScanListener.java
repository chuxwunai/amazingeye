package eye.restul.scan;

import java.util.List;

public interface ClassPathScanListener {
	
	void listen(List<String> classes);

}
