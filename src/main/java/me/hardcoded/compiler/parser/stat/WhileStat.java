package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.serial.TreeType;

public class WhileStat extends Stat {
	private Expr condition;
	private Stat body;
	
	public WhileStat(ISyntaxPos syntaxPos, Expr condition, Stat body) {
		super(syntaxPos);
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
