package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A break statement
 * 
 * <pre>
 * Valid syntax:
 *   'break' ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class BreakStat extends Stat {
	private BreakStat(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.BREAK;
	}
	
	@Override
	public String toString() {
		return "break;";
	}
	
	public static BreakStat get(Token start) {
		return new BreakStat(start);
	}
}
