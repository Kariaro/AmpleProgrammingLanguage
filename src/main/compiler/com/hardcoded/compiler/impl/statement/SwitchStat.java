package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A switch statement
 * 
 * <pre>
 * Valid syntax:
 *   'switch' '(' [expr] ')' [case]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class SwitchStat extends Stat {
	private SwitchStat(Token token) {
		super(token, true);
	}

	@Override
	public Type getType() {
		return Type.SWITCH;
	}
	
	@Override
	public String toString() {
		return String.format("switch(%s);", list.get(0));
	}
	
	public static SwitchStat get(Token token) {
		return new SwitchStat(token);
	}
}
