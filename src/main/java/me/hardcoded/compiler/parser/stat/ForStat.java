package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.type.TreeType;

public class ForStat extends Stat {
	private Stat start;
	private Expr condition;
	private Expr action;
	private Stat body;
	
	public ForStat(Stat start, Expr condition, Expr action, Stat body, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.start = start;
		this.condition = condition;
		this.action = action;
		this.body = body;
	}
	
	public Stat getStart() {
		return start;
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
		// TODO: If the for loop only modifies its own values we could optimize it away
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.FOR;
	}
}
