package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A if else statement
 * 
 * <pre>
 * Valid syntax:
 *   'if' '(' [expr] ')' [stat] 'else' [stat]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class IfElseStat extends Stat {
	private IfElseStat(Token token) {
		super(token, true);
	}
	
	@Override
	public Type getType() {
		return Type.IF_ELSE;
	}
	
	@Override
	public String toString() {
		if(list.isEmpty()) return "if else([INVALID]);";
		return String.format("if else(%s);", list.get(0));
	}
	
	public static IfElseStat get(Token token) {
		return new IfElseStat(token);
	}
}
