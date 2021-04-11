package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A case statement
 * 
 * <pre>
 * Valid syntax:
 *   'case' [expr] ':' [stat]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class CaseStat extends Stat {
	private CaseStat(Token token) {
		super(token, true);
	}

	@Override
	public Type getType() {
		return Type.CASE;
	}
	
	@Override
	public String toString() {
		return String.format("case %s:", list.get(0));
	}
	
	public static CaseStat get(Token token) {
		return new CaseStat(token);
	}
}
