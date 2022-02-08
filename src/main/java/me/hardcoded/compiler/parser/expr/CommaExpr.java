package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.TreeType;

import java.util.List;

public class CommaExpr extends Expr {
	private List<Expr> values;
	
	public CommaExpr(List<Expr> values, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.values = values;
	}
	
	public List<Expr> getValues() {
		return values;
	}
	
	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}
	
	public Expr getLast() {
		if (values.isEmpty()) {
			return null;
		}
		
		return values.get(values.size() - 1);
	}
	
	public void setLast(Expr expr) {
		if (values.isEmpty()) {
			values.add(expr);
		} else {
			values.set(values.size() - 1, expr);
		}
	}
	
	@Override
	public boolean isPure() {
		for (Expr expr : values) {
			if (!expr.isPure()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.COMMA;
	}
}
