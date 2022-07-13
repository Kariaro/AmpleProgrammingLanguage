package me.hardcoded.compiler.errors;

public class InstException extends RuntimeException {
	public InstException(String format, Object... args) {
		super(String.format(format, args));
	}
}
