package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Operator;
import me.hardcoded.compiler.parser.type.OperatorType;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class UnaryExpr extends Expr {
	private Expr value;
	private Operator operator;
	
	public UnaryExpr(ISyntaxPosition syntaxPosition, Operator operator, Expr value) {
		super(syntaxPosition);
		this.value = value;
		this.operator = operator;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public Expr getValue() {
		return value;
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean isPure() {
		return value.isPure();
	}
	
	@Override
	public ValueType getType() {
		return value.getType();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.UNARY;
	}
	
	@Override
	public String toString() {
		if (operator.getOperatorType() == OperatorType.Prefix) {
			return "(" + operator.getName() + value + ")";
		}
		
		return "(" + value + operator.getName() + ")";
	}
}
