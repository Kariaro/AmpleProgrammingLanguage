package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Associativity;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.ValueType;

public class UnaryExpr extends Expr {
	private Expr value;
	private Operation operation;
	
	public UnaryExpr(ISyntaxPos syntaxPos, Operation operation, Expr value) {
		super(syntaxPos);
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
		if (operation.getAssociativity() == Associativity.Right) {
			return "(" + operation.getName() + value + ")";
		}
		
		return "(" + value + operation.getName() + ")";
	}
}
