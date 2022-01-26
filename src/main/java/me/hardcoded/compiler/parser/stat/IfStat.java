package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;

public class IfStat extends Stat {
	private Expr condition;
	private Stat body;
	private Stat elseBody;
	
	public IfStat(Expr condition, Stat body, Stat elseBody, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.condition = condition;
		this.body = body;
		this.elseBody = elseBody;
	}
	
	public Expr getCondition() {
		return condition;
	}
	
	public Stat getBody() {
		return body;
	}
	
	public Stat getElseBody() {
		return elseBody;
	}
	
	public boolean hasElseBody() {
		return !(elseBody instanceof EmptyStat);
	}
	
	@Override
	public boolean isEmpty() {
		// TODO: If condition is pure and that body scope is empty
		//       then this statement is also empty
		return elseBody.isEmpty() && body.isEmpty();
	}
	
	@Override
	public boolean isPure() {
		// TODO: Make sure that we calculate the condition
		return condition.isPure() && body.isPure() && elseBody.isPure();
	}
}
