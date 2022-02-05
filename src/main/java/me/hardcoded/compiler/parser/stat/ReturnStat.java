package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.type.TreeType;

public class ReturnStat extends Stat {
	private Expr expr;
	
	public ReturnStat(Expr expr, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.expr = expr;
	}
	
	public Expr getValue() {
		return expr;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return expr.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.RETURN;
	}
}
