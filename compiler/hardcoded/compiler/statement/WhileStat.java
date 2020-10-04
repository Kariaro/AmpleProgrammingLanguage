package hardcoded.compiler.statement;

import hardcoded.compiler.expression.Expression;

public class WhileStat extends NestedStat {
	public WhileStat() {
		super(2);
	}
	
	public Expression getCondition() {
		ExprStat stat = get(0);
		return stat == null ? null:(stat.expr());
	}
	
	public Statement getBody() {
		return get(1);
	}
	
	public void setCondition(Expression expr) {
		set(0, new ExprStat(expr));
	}
	
	public void setBody(Statement stat) {
		set(1, stat);
	}
	
	public String asString() { return "WHILE"; }
	public String toString() { return "while(" + getCondition() + ");"; }
}