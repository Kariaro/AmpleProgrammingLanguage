package hardcoded.configuration;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationTest {
	private Map<String, Object> map;
	
	public ConfigurationTest() {
		map = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T)map.get(key);
	}
	
	public void set(String key, Object object) {
		map.put(key, object);
	}
	
	public String toString() {
		return map.toString();
	}
}
