package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Expression;
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
public class ExprStat extends Stat {
	protected final List<Expression> list;
	
	private ExprStat(Token token) {
		super(token);
		this.list = new ArrayList<>();
	}
	
	@Override
	public Type getType() {
		return Type.EXPR;
	}
	
	public List<Expression> getExpressions() {
		return list;
	}
	
	public void add(Expression expr) {
		list.add(expr);
	}
	
	@Override
	public String toString() {
		return String.format("%s", list);
	}
	
	public static ExprStat get(Token token) {
		return new ExprStat(token);
	}
}
