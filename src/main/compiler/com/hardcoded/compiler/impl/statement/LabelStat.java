package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A label statement
 * 
 * <pre>
 * Valid syntax:
 *   [name] ':'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class LabelStat extends Stat {
	private LabelStat(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.LABEL;
	}
	
	@Override
	public String toString() {
		return String.format("%s:", token);
	}
	
	public static LabelStat get(Token start) {
		return new LabelStat(start);
	}
}
