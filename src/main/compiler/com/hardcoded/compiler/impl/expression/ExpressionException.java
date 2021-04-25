package com.hardcoded.compiler.impl.expression;

/**
 * A expression exception.
 * 
 * This is thrown when a expression is invalid
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ExpressionException extends RuntimeException {
	private static final long serialVersionUID = -7982101699690120444L;
	
	public ExpressionException(String format, Object... args) {
		super(String.format(format, args));
	}
}
