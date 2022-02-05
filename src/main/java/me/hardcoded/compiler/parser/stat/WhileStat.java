package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.type.TreeType;

public class WhileStat extends Stat {
	private Expr condition;
	private Stat body;
	
	public WhileStat(Expr condition, Stat body, ISyntaxPosition syntaxPosition) {
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
		return condition.isPure() && body.isEmpty();
	}
	
	@Override
	public boolean isPure() {
		return condition.isPure() && body.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.WHILE;
	}
}
