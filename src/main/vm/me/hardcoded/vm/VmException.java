package me.hardcoded.vm;

public class VmException extends RuntimeException {
	private static final long serialVersionUID = 428300432824340585L;
	
	public VmException(String message) {
		super(message);
	}
}
