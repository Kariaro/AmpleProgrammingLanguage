package hardcoded.compiler.statement;

import hardcoded.compiler.expression.Expression;

public class IfStat extends NestedStat {
	public IfStat() {
		super(3);
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
	
	public void setCondition(Expression expr) {
		set(0, new ExprStat(expr));
	}
	
	public Statement setBody(Statement stat) {
		return set(1, stat);
	}
	
	public Statement setElseBody(Statement stat) {
		return set(2, (stat == null ? Statement.EMPTY:stat));
	}
	
	public boolean hasElseBody() {
		return get(2) != EMPTY;
	}
	
	public String asString() { return "IF"; }
	public String toString() { return "if(" + getCondition() + ");"; }
}