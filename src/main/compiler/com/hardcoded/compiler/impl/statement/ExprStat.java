package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A expression statement
 * 
 * <pre>
 * Valid syntax:
 *   [expr] ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ExprStat implements Statement {
	protected final List<Expression> list;
	protected final Token token;
	
	private ExprStat(Token token) {
		this.list = new ArrayList<>();
		this.token = token;
	}

	@Override
	public Type getType() {
		return Type.EXPR;
	}

	@Override
	public List<Statement> getStatements() {
		return Collections.emptyList();
	}
	
	public List<Expression> getExpressions() {
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
	
	public void add(Expression expr) {
		list.add(expr);
	}
	
	@Override
	public String toString() {
		return String.format("%s;", list);
	}
	
	public static ExprStat get(Token token) {
		return new ExprStat(token);
	}
}
