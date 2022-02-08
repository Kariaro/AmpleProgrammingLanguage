package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.TreeType;

import java.util.ArrayList;
import java.util.List;

public class ConditionalExpr extends Expr {
	private Operation operation;
	private List<Expr> values;
	
	public ConditionalExpr(Operation operation, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.operation = operation;
		this.values = new ArrayList<>();
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public void addElement(Expr expr) {
		values.add(expr);
	}
	
	public List<Expr> getValues() {
		return values;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		// If all children are pure this is also pure
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.CONDITIONAL;
	}
}
