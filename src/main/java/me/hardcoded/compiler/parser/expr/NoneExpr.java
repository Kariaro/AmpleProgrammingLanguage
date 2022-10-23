package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class NoneExpr extends Expr {
	public NoneExpr(ISyntaxPos syntaxPos) {
		super(syntaxPos);
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
	@Override
	public ValueType getType() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.NONE;
	}
	
	@Override
	public String toString() {
		return "(<none>)";
	}
}
