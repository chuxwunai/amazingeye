package eye.restul.server;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public interface RestfulConstants {
	Set<String> ALLOW_METHODS = new LinkedHashSet<String>(Arrays.asList("GET", "POST", "PUT", "DELETE"));

}
