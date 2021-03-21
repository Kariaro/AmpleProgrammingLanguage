package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
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
public class DefineStat implements Statement {
	protected final List<Statement> list;
	protected final Token type;
	protected final Token name;
	
	private DefineStat(Token type, Token name) {
		this.list = new ArrayList<>();
		this.type = type;
		this.name = name;
	}
	
	@Override
	public Type getType() {
		return Type.DEFINE;
	}

	@Override
	public List<Statement> getStatements() {
		return list;
	}
	
	@Override
	public int getLineIndex() {
		return type.line;
	}
	
	@Override
	public int getColumnIndex() {
		return type.column;
	}
	
	public Token getValueType() {
		return type;
	}
	
	public Token getName() {
		return name;
	}
	
	public void add(Statement stat) {
		list.add(stat);
	}
	
	@Override
	public String toString() {
		return String.format("%s %s;", type, name);
	}
	
	public static DefineStat get(Token type, Token name) {
		return new DefineStat(type, name);
	}
}
