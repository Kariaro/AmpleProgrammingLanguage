package me.hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import me.hardcoded.compiler.expression.Expression;
import me.hardcoded.utils.StringUtils;

public class ExprStat extends Statement {
	public List<Expression> list = new ArrayList<>();
	
	public ExprStat(Expression expr) {
		super(false);
		list.add(expr);
	}
	
	public Expression expr() {
		return list.isEmpty() ? null:list.get(0);
	}
	
	@Override
	public String asString() {
		return toString();
	}
	
	@Override
	public Object[] asList() {
		return list.toArray();
	}
	
	@Override
	public String toString() {
		return StringUtils.join(" ", list);
	}
}