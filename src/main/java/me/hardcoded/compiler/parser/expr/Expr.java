package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.stat.Stat;
import me.hardcoded.compiler.parser.type.ValueType;

public abstract class Expr extends Stat {
	public Expr(ISyntaxPos syntaxPos) {
		super(syntaxPos);
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	/**
	 * Returns the type of this expression
	 */
	public abstract ValueType getType();
	
	@Override
	public abstract TreeType getTreeType();
}
