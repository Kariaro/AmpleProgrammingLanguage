package me.hardcoded.compiler.statement;

import me.hardcoded.compiler.expression.Expression;
import me.hardcoded.compiler.impl.ISyntaxPosition;

public class IfStat extends NestedStat {
	public IfStat(Expression condition, Statement body, Statement elseBody, ISyntaxPosition syntaxPosition) {
		super(3);
		set(0, new ExprStat(condition));
		set(1, body);
		set(2, elseBody);
		this.syntaxPosition = syntaxPosition;
	}
	
	public Expression getCondition() {
		return ((ExprStat)get(0)).expr();
	}
	
	public Statement getBody() {
		return get(1);
	}
	
	public Statement getElseBody() {
		return get(2);
	}
	
	public boolean hasElseBody() {
		Statement stat = getElseBody();
		// TODO: return stat.size() > 0;
		return !stat.isEmptyStat() && stat.hasElements() && stat.size() > 0;
	}

	@Override
	public Object[] asList() {
		if(hasElseBody()) return super.asList();
		return new Object[] { get(0), get(1) };
	}
	
	@Override
	public String asString() {
		return "IF";
	}
	
	@Override
	public String toString() {
		return "if(%s)".formatted(getCondition());
	}
}