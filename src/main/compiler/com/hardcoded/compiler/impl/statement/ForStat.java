package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A for statement
 * 
 * <pre>
 * Valid syntax:
 *   'for' '(' [define]? ; [expr]? ; [expr]? ')' [stat]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ForStat extends Stat {
	private ForStat(Token token) {
		super(token, true);
	}
	
	@Override
	public Type getType() {
		return Type.FOR;
	}
	
	@Override
	public String toString() {
		if(list.size() < 3) return "for([INVALID]);";
		return String.format("for(%s%s%s);", list.get(0), list.get(1), list.get(2));
	}
	
	public static ForStat get(Token token) {
		return new ForStat(token);
	}
}
