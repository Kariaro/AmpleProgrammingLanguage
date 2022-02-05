package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.stat.Stat;
import me.hardcoded.compiler.parser.type.TreeType;

public abstract class Expr extends Stat {
	public Expr(ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
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
	public abstract TreeType getTreeType();
}
