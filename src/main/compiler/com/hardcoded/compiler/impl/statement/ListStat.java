package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A list of stats
 * 
 * <pre>
 * Valid syntax:
 *   '{' [stat] '}'
 * </pre>
 * @author HardCoded
 * @since 0.2.0
 */
public class ListStat implements Statement {
	protected final List<Statement> list;
	protected final Token token;
	
	private ListStat(Token token) {
		this.list = new ArrayList<>();
		this.token = token;
	}
	
	@Override
	public Type getType() {
		return Type.NONE;
	}
	
	@Override
	public List<Statement> getStatements() {
		return list;
	}
	
	@Override
	public int getLineIndex() {
		return token.line;
	}
	
	@Override
	public int getColumnIndex() {
		return token.column;
	}
	
	public void add(Statement stat) {
		list.add(stat);
	}
	
	@Override
	public String toString() {
		return String.format("{%s}", list);
	}
	
	public static ListStat get(Token token) {
		return new ListStat(token);
	}
}
