package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A while statement
 * 
 * <pre>
 * Valid syntax:
 *   'while' '(' [expr] ')' [stat]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class WhileStat extends Stat {
	private WhileStat(Token token) {
		super(token, true);
	}
	
	@Override
	public Type getType() {
		return Type.WHILE;
	}
	
	@Override
	public String toString() {
		if(list.isEmpty()) return "while([INVALID]);";
		return String.format("while(%s);", list.get(0));
	}
	
	public static WhileStat get(Token token) {
		return new WhileStat(token);
	}
}
