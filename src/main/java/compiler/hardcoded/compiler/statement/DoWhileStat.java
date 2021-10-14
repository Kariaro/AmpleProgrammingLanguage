package hardcoded.compiler.statement;

import hardcoded.compiler.expression.Expression;

public class DoWhileStat extends NestedStat {
	public DoWhileStat(Expression condition, Statement body) {
		super(2);
		set(0, new ExprStat(condition));
		set(1, body);
	}
	
	public Expression getCondition() {
		ExprStat stat = (ExprStat)get(0);
		return stat == null ? null:(stat.expr());
	}
	
	public Statement getBody() {
		return get(1);
	}
	
	@Override
	public String asString() {
		return "DoWhile";
	}
	
	@Override
	public String toString() {
		return "do { ... } while(%s);".formatted(getCondition());
	}
}