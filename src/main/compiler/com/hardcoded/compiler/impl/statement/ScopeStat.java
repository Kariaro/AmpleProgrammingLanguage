package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A list of stats
 * 
 * <pre>
 * Valid syntax:
 *   '{' [stat] '}'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ScopeStat extends Stat {
	private ScopeStat(Token token) {
		super(token, true);
	}
	
	@Override
	public Type getType() {
		return Type.SCOPE;
	}
	
	@Override
	public String toString() {
		return String.format("{%s}", list);
	}
	
	public static ScopeStat get(Token token) {
		return new ScopeStat(token);
	}
}
