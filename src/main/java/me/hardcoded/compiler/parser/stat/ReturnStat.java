package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.expr.NoneExpr;
import me.hardcoded.compiler.parser.serial.TreeType;

public class ReturnStat extends Stat {
	private Expr value;
	
	public ReturnStat(ISyntaxPos syntaxPos, Expr value) {
		super(syntaxPos);
		this.value = value;
	}
	
	public Expr getValue() {
		return value;
	}
	
	public boolean hasValue() {
		return !(value instanceof NoneExpr);
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return value.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.RETURN;
	}
}
