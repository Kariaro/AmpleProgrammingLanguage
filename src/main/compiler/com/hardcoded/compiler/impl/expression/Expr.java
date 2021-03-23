package com.hardcoded.compiler.impl.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.lexer.Token;
import com.hardcoded.utils.StringUtils;

/**
 * A abstract expression implementation
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public abstract class Expr implements Expression {
	protected final List<Expression> list;
	protected final Token token;
	protected Token end;
	
	protected Expr(Token token) {
		this(token, false);
	}
	
	protected Expr(Token token, boolean no_list) {
		this.list = no_list ? Collections.emptyList():new ArrayList<>();
		this.token = token;
		this.end = Token.EMPTY;
	}
	
	public Expr add(Expression expr) {
		list.add(expr);
		return this;
	}
	
	public Expr add(Expression... array) {
		for(Expression e : array)
			list.add(e);
		
		return this;
	}
	
	public Expression last() {
		return list.get(list.size() - 1);
	}
	
	public final Token getToken() {
		return token;
	}
	
	public final Token getEnd() {
		return end;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Expr> T end(Token end) {
		this.end = end;
		return (T)this;
	}
	
	@Override
	public List<Expression> getExpressions() {
		return list;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s)", getType().name().toLowerCase(), StringUtils.join(", ", list));
	}
}
