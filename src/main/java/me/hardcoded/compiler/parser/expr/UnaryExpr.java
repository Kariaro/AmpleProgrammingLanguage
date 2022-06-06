package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.OperationType;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class UnaryExpr extends Expr {
	private Expr value;
	private Operation operation;
	
	public UnaryExpr(ISyntaxPosition syntaxPosition, Operation operation, Expr value) {
		super(syntaxPosition);
		this.value = value;
		this.operation = operation;
	}
	
	public Operation getOperation() {
		return operation;
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
		if (operation.getOperationType() == OperationType.Prefix) {
			return "(" + operation.getName() + value + ")";
		}
		
		return "(" + value + operation.getName() + ")";
	}
}
