package hardcoded.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * A configuration file
 * 
 * @author HardCoded
 * @since v0.2
 */
public class Config {
	private Map<AmpleOptions, Object> map;
	
	public Config() {
		map = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(AmpleOptions key) {
		return (T)map.get(key);
	}
	
	public void set(AmpleOptions key, Object object) {
		if(key == null || object == null) throw new NullPointerException();
		if(key.type.isInstance(object.getClass())) throw new IllegalArgumentException("The object '" + object.getClass() + "' cannot be cast to a '" + key.type + "'");
		map.put(key, object);
	}
	
	public String toString() {
		return map.toString();
	}
}
