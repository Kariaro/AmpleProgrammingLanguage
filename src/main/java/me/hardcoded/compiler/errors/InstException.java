package me.hardcoded.compiler.errors;

public class InstException extends CompilerException {
	public InstException(String message) {
		super(message);
	}
	
	public InstException(String message, Throwable cause) {
		super(message, cause);
	}
}
