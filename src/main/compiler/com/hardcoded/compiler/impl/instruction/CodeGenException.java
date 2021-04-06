package com.hardcoded.compiler.impl.instruction;

public class CodeGenException extends RuntimeException {
	private static final long serialVersionUID = 4632655138891070918L;
	
	public CodeGenException(String format, Object... args) {
		super(String.format(format, args));
	}
}
