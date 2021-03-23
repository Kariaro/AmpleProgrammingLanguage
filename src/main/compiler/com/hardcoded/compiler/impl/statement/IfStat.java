package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A if statement
 * 
 * <pre>
 * Valid syntax:
 *   'if' '(' [expr] ')' [stat]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class IfStat extends Stat {
	private IfStat(Token token) {
		super(token, true);
	}

	@Override
	public Type getType() {
		return Type.IF;
	}
	
	@Override
	public String toString() {
		if(list.isEmpty()) return "if([INVALID]);";
		return String.format("if(%s);", list.get(0));
	}
	
	public static IfStat get(Token token) {
		return new IfStat(token);
	}
}
