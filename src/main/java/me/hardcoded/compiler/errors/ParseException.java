package me.hardcoded.compiler.errors;

public class ParseException extends Exception {
	public ParseException(String format, Object... args) {
		super(String.format(format, args));
	}
}
