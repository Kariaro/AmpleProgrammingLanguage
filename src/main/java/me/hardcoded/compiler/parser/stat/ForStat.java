package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.serial.TreeType;

public class ForStat extends Stat {
	private Stat initializer;
	private Expr condition;
	private Expr action;
	private Stat body;
	
	public ForStat(ISyntaxPosition syntaxPosition, Stat initializer, Expr condition, Expr action, Stat body) {
		super(syntaxPosition);
		this.initializer = initializer;
		this.condition = condition;
		this.action = action;
		this.body = body;
	}
	
	public Stat getInitializer() {
		return initializer;
	}
	
	public Expr getCondition() {
		return condition;
	}
	
	public Expr getAction() {
		return action;
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
		return TreeType.FOR;
	}
}
