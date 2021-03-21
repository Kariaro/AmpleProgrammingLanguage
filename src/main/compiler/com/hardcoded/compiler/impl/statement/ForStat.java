package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
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
public class ForStat implements Statement {
	protected final List<Statement> list;
	protected final Token token;
	
	private ForStat(Token token) {
		this.list = new ArrayList<>();
		this.token = token;
	}

	@Override
	public Type getType() {
		return Type.FOR;
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
		if(list.size() < 3) return "for([INVALID]);";
		return String.format("for(%s%s%s);", list.get(0), list.get(1), list.get(2));
	}
	
	public static ForStat get(Token token) {
		return new ForStat(token);
	}
}
