package eye.restul.scan;

import java.util.LinkedHashMap;

public class QueryVariableMap extends LinkedHashMap<String,String[]> {
	
	private static final long serialVersionUID = 1L;
	
	private void addParameter(String name, String value) {
		String[] values = get(name);
		if (values == null) {
			values = new String[] { value };
		} else {
			int length = values.length;
			String[] newValues = new String[length + 1];
			System.arraycopy(values, 0, newValues, 0, length);
			values = newValues;
			values[length] = value;
		}
		put(name, values);
	}
	
	public static QueryVariableMap parseFrom(String queryString) {
		QueryVariableMap result = new QueryVariableMap();
		if (queryString != null && queryString.length() > 0) {
			char[] buf = queryString.toCharArray();
			int i = 0, length = buf.length;
			do {
				int j = i, k = i;
				while (j < length && buf[j] != '=') j++;
				while (k < length && buf[k] != '&') k++;
				if (j == length) break;
				if (j < k) {
					String name = String.valueOf(buf, i, j - i);
					String value = String.valueOf(buf, j + 1, k - j - 1);
					result.addParameter(name, value);
				}
				i = k + 1;
			} while (i < length);
		}
		return result;
	}

}
