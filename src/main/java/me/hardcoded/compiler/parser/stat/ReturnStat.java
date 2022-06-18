package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.serial.TreeType;

public class ReturnStat extends Stat {
	private Expr value;
	
	public ReturnStat(ISyntaxPosition syntaxPosition, Expr value) {
		super(syntaxPosition);
		this.value = value;
	}
	
	public Expr getValue() {
		return value;
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
