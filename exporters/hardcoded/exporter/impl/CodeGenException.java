package hardcoded.exporter.impl;

public class CodeGenException extends RuntimeException {
	private static final long serialVersionUID = 2329722989620905682L;
	
	private final String message;
	private final Throwable cause;
	
	public CodeGenException() {
		this(null, null);
	}
	
	public CodeGenException(String message) {
		this(message, null);
	}
	
	public CodeGenException(Throwable cause) {
		this(null, cause);
	}
	
	public CodeGenException(String message, Throwable cause) {
		this.message = message;
		this.cause = cause;
	}
	
	public String getMessage() {
		return message;
	}
	
	public synchronized Throwable getCause() {
		return cause;
	}
}
