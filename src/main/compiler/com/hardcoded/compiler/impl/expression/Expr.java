package com.hardcoded.compiler.impl.expression;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.impl.context.NonNullList;
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
	protected int start_offset;
	protected int end_offset;
	
	protected Expr(Token token) {
		this(token, true);
	}
	
	protected Expr(Token token, boolean has_list) {
		this.list = has_list ? new NonNullList<>(EmptyExpr.get()):Collections.emptyList();
		this.start_offset = token.offset;
		this.end_offset = token.offset;
	}
	
	public Expr add(Expression expr) {
		list.add(expr);
		return this;
	}
	
	public Expr add(Expression... array) {
		for(Expression e : array) add(e);
		return this;
	}
	
	public Expression last() {
		return list.get(list.size() - 1);
	}
	
	public Expression get(int index) {
		return list.get(index);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Expr> T end(Token end) {
		this.end_offset = end.offset + end.value.length();
		return (T)this;
	}
	
	@Override
	public int getStartOffset() {
		return start_offset;
	}
	
	@Override
	public int getEndOffset() {
		return end_offset;
	}
	
	public Expr setLocation(int start, int end) {
		this.start_offset = start;
		this.end_offset = end;
		return this;
	}
	
	@Override
	public List<Expression> getExpressions() {
		return list;
	}
	
	@Override
	public boolean isPure() {
		switch(getType()) {
			case SET:
			case CALL:
				return false;
			
			default: {
				for(Expression e : list) {
					if(!e.isPure()) return false;
				}
				
				return true;
			}
		}
	}
	
	@Override
	public boolean isConstant() {
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s)", getType().name().toLowerCase(), StringUtils.join(", ", list));
	}
}
