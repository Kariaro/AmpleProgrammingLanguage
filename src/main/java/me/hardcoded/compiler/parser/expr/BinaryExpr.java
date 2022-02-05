package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.TreeType;

public class BinaryExpr extends Expr {
	private Expr left;
	private Expr right;
	private Operation operation;
	
	public BinaryExpr(Expr left, Expr right, Operation operation, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.left = left;
		this.right = right;
		this.operation = operation;
	}
	
	public Expr getLeft() {
		return left;
	}
	
	public Expr getRight() {
		return right;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		switch (operation) {
			case ASSIGN -> {
				return false;
			}
		}
		return left.isPure() && right.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.BINARY;
	}
}
