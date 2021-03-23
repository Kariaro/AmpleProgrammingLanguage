package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A list of stats
 * 
 * <pre>
 * Valid syntax:
 *   '{' [stat] '}'
 * </pre>
 * @author HardCoded
 * @since 0.2.0
 */
public class ListStat extends Stat {
	private ListStat(Token token) {
		super(token, true);
	}
	
	@Override
	public Type getType() {
		return Type.NONE;
	}
	
	@Override
	public String toString() {
		return String.format("{%s}", list);
	}
	
	public static ListStat get(Token token) {
		return new ListStat(token);
	}
}
