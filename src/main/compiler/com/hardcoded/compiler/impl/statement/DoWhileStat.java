package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A do while statement
 * 
 * <pre>
 * Valid syntax:
 *   'do' '{' [stat] '}' 'while' '(' [expr] ')' ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class DoWhileStat extends Stat {
	private DoWhileStat(Token token) {
		super(token, true);
	}

	@Override
	public Type getType() {
		return Type.DO_WHILE;
	}
	
	@Override
	public String toString() {
		if(list.size() < 2) return "do while([INVALID])";
		return String.format("do while(%s);", list.get(1));
	}
	
	public static DoWhileStat get(Token token) {
		return new DoWhileStat(token);
	}
}
