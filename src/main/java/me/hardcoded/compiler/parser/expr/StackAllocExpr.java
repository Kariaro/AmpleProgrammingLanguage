package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.ValueType;

public class StackAllocExpr extends Expr {
	private ValueType type;
	private int size;
	private Expr value;
	
	public StackAllocExpr(ISyntaxPosition syntaxPosition, ValueType type, int size, Expr value) {
		super(syntaxPosition);
		this.size = size;
		this.type = type;
		this.value = value;
	}
	
	public Expr getValue() {
		return value;
	}
	
	public int getSize() {
		return size;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	@Override
	public ValueType getType() {
		return type;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.STACK_ALLOC;
	}
	
	@Override
	public String toString() {
		return "stack_alloc<" + type + ", " + size + ">( " + value + " )";
	}
}
