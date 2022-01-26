package me.hardcoded.utils.error;

public class CodeGenException extends RuntimeException {
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
	
	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}
}
