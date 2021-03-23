package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A goto statement
 * 
 * <pre>
 * Valid syntax:
 *   'goto' [label] ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class GotoStat extends Stat {
	protected final Token label;
	
	private GotoStat(Token token, Token label) {
		super(token);
		this.label = label;
	}
	
	@Override
	public Type getType() {
		return Type.GOTO;
	}
	
	public Token getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return String.format("goto %s;", label);
	}
	
	public static GotoStat get(Token token, Token label) {
		return new GotoStat(token, label);
	}
}
