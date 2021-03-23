package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A return statement
 * 
 * <pre>
 * Valid syntax:
 *   'return' [expr] ';'
 *   'return' ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ReturnStat extends Stat {
	private ReturnStat(Token token) {
		super(token, true);
	}
	
	@Override
	public Type getType() {
		return Type.RETURN;
	}
	
	@Override
	public String toString() {
		if(list.isEmpty()) return "return;";
		return String.format("return %s;", list);
	}
	
	public static ReturnStat get(Token token) {
		return new ReturnStat(token);
	}
}
