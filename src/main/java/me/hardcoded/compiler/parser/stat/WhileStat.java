package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.serial.TreeType;

public class WhileStat extends Stat {
	private Expr condition;
	private Stat body;
	
	public WhileStat(ISyntaxPosition syntaxPosition, Expr condition, Stat body) {
		super(syntaxPosition);
		this.condition = condition;
		this.body = body;
	}
	
	public Expr getCondition() {
		return condition;
	}
	
	public Stat getBody() {
		return body;
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
	public TreeType getTreeType() {
		return TreeType.WHILE;
	}
}
