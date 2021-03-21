package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A if statement
 * 
 * <pre>
 * Valid syntax:
 *   'if' '(' [expr] ')' [stat]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class IfStat implements Statement {
	protected final List<Statement> list;
	protected final Token token;
	
	private IfStat(Token token) {
		this.list = new ArrayList<>();
		this.token = token;
	}

	@Override
	public Type getType() {
		return Type.IF;
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
		if(list.isEmpty()) return "if([INVALID]);";
		return String.format("if(%s);", list.get(0));
	}
	
	public static IfStat get(Token token) {
		return new IfStat(token);
	}
}
