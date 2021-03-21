package com.hardcoded.compiler.parsetree;

public class ParseTreeException extends RuntimeException {
	private static final long serialVersionUID = 1259034092578324451L;
	
	public ParseTreeException(String format, Object... args) {
		super(String.format(format, args));
	}
}
