package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
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
public class WhileStat implements Statement {
	protected final List<Statement> list;
	protected final Token token;
	
	private WhileStat(Token token) {
		this.list = new ArrayList<>();
		this.token = token;
	}

	@Override
	public Type getType() {
		return Type.WHILE;
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
		if(list.isEmpty()) return "while([INVALID]);";
		return String.format("while(%s);", list.get(0));
	}
	
	public static WhileStat get(Token token) {
		return new WhileStat(token);
	}
}
