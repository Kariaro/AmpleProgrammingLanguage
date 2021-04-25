package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.lexer.Token;

/**
 * A keyword statement
 * 
 * <pre>
 * Valid syntax:
 *   'keyword' '(' [keyword-args] ')' '{' [keyword-body] '}'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class KeywordStat extends Stat {
	protected final Token name;
	protected final List<Token> args = new ArrayList<>();
	protected final List<Token> body = new ArrayList<>();
	
	private KeywordStat(Token token, Token name) {
		super(token, false);
		this.name = name;
	}
	
	@Override
	public Type getType() {
		return Type.CLASS;
	}
	
	public Token getName() {
		return name;
	}
	
	public List<Token> getArguments() {
		return args;
	}
	
	public List<Token> getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		return String.format("class %s;", name);
	}
	
	public static KeywordStat get(Token token, Token name) {
		return new KeywordStat(token, name);
	}
}
