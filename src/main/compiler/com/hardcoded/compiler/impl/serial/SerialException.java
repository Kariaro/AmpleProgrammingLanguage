package com.hardcoded.compiler.impl.serial;

public class SerialException extends RuntimeException {
	private static final long serialVersionUID = -8865888688489050607L;
	
	public SerialException(String message) {
		super(message);
	}
	
	public SerialException(Throwable cause) {
		super(cause);
	}
}
