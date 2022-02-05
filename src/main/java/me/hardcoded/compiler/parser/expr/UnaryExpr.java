package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.lexer.Token;

public class UnaryExpr extends Expr {
	private Expr expr;
	private Operation operation;
	
	public UnaryExpr(Expr expr, Operation operation, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.expr = expr;
		this.operation = operation;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public Expr getValue() {
		return expr;
	}
	
	public boolean isPrefix() {
		return operation.isPrefix();
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		switch (operation) {
			case POST_DECREMENT, PRE_DECREMENT, POST_INCREMENT, PRE_INCREMENT, DEREFERENCE, REFERENCE -> {
				return false;
			}
		}
		
		return expr.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.UNARY;
	}
}
