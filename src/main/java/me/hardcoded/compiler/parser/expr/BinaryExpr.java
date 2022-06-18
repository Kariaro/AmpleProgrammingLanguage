package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class BinaryExpr extends Expr {
	private Expr left;
	private Expr right;
	private Operation operation;
	
	public BinaryExpr(ISyntaxPosition syntaxPosition, Operation operation, Expr left, Expr right) {
		super(syntaxPosition);
		this.left = left;
		this.right = right;
		this.operation = operation;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public Expr getLeft() {
		return left;
	}
	
	public Expr getRight() {
		return right;
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean isPure() {
		return left.isPure() && right.isPure();
	}
	
	@Override
	public ValueType getType() {
		// Both left and right must have the same value type
		// throw new UnsupportedOperationException();
		// TODO: Validate
		return left.getType();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.BINARY;
	}
	
	@Override
	public String toString() {
		return "(" + left + " " + operation.getName() + " " + right + ")";
	}
}
