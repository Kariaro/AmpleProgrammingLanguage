package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A define statment
 * 
 * <pre>
 * Valid syntax:
 *   [type] [name] '=' [expr] ';'
 *   [type] [name] ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class DefineStat extends Stat {
	protected final Token type;
	protected final Token name;
	
	private DefineStat(Token type, Token name) {
		super(type, true);
		this.type = type;
		this.name = name;
	}
	
	@Override
	public Type getType() {
		return Type.DEFINE;
	}
	
	public Token getValueType() {
		return type;
	}
	
	public Token getName() {
		return name;
	}
	
	@Override
	public String toString() {
		if(list.isEmpty()) {
			return String.format("%s %s;", type, name);
		}
		
		return String.format("%s %s = %s;", type, name, list.get(0));
	}
	
	public static DefineStat get(Token type, Token name) {
		return new DefineStat(type, name);
	}
}
