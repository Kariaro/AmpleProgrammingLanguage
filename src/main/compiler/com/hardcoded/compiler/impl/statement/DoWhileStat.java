package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
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
public class DoWhileStat implements Statement {
	protected final List<Statement> list;
	protected final Token token;
	
	private DoWhileStat(Token token) {
		this.list = new ArrayList<>();
		this.token = token;
	}

	@Override
	public Type getType() {
		return Type.DO_WHILE;
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
		if(list.size() < 2) return "do while([INVALID])";
		return String.format("do while(%s);", list.get(1));
	}
	
	public static DoWhileStat get(Token token) {
		return new DoWhileStat(token);
	}
}
