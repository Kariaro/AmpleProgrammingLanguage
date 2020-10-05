package hardcoded.compiler.statement;

import hardcoded.compiler.expression.Expression;

public class IfStat extends NestedStat {
	public IfStat() {
		super(3);
	}
	
	public IfStat(Expression condition, Statement body, Statement elseBody) {
		super(3);
		set(0, new ExprStat(condition));
		set(1, body);
		set(2, elseBody);
	}
	
	public Expression getCondition() {
		ExprStat stat = get(0);
		return stat == null ? null:(stat.expr());
	}
	
	public Statement getBody() {
		return get(1);
	}
	
	public Statement getElseBody() {
		return get(2);
	}
	
	public boolean hasElseBody() {
		return get(2) != EMPTY;
	}
	
	public String asString() { return "IF"; }
	public String toString() { return "if(" + getCondition() + ");"; }
}