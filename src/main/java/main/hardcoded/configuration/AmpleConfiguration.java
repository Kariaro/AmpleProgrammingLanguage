package hardcoded.configuration;

import java.io.File;
import java.util.HashMap;

/**
 * Checking how a properties system would work.
 * 
 */
abstract class AmpleConfiguration {
	private HashMap<String, Object> properties;
	protected String errorMessage;
	protected int errorCode;
	
	protected AmpleConfiguration() {
		properties = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T get(String key) {
		return (T)properties.get(key);
	}
	
	protected final void set(String key, Object object) {
		properties.put(key, object);
	}
	
	public File getFile(String key) { return get(key); }
	public void setFile(String key, File file) { set(key, file); }
	
	public abstract boolean isValid();
	
	/**
	 * Always return {@code false}
	 * 
	 * @param errorCode
	 * @param errorMessage
	 * @return {@code false}
	 */
	protected final boolean setMessage(int errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		return false;
	}
	
	/**
	 * @return the latest error message
	 */
	public final String getLastError() {
		return errorMessage;
	}
	
	/**
	 * @return the latest error code
	 */
	public final int getLastCode() {
		return errorCode;
	}
}
