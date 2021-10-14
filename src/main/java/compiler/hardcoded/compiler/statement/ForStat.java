package hardcoded.compiler.statement;

import java.util.Objects;

import hardcoded.compiler.expression.Expression;

public class ForStat extends NestedStat {
	public ForStat() {
		super(4);
	}
	
	public Statement getVariables() {
		return get(0);
	}
	
	public Expression getCondition() {
		ExprStat stat = (ExprStat)get(1);
		return stat == null ? null:(stat.expr());
	}
	
	public Expression getAction() {
		ExprStat stat = (ExprStat)get(2);
		return stat == null ? null:(stat.expr());
	}
	
	public Statement getBody() {
		return get(3);
	}
	
	public void setVariables(Statement stat) {
		set(0, stat);
	}
	
	public void setCondition(Expression expr) {
		set(1, new ExprStat(expr));
	}
	
	public void setAction(Expression expr) {
		set(2, new ExprStat(expr));
	}
	
	public void setBody(Statement stat) {
		set(3, stat);
	}
	
	@Override
	public String asString() {
		return "FOR";
	}
	
	@Override
	public String toString() {
		String vars = Objects.toString(getVariables(), "");
		String cond = Objects.toString(getCondition(), null);
		String acts = Objects.toString(getAction(), null);
		
		return "for(%s;%s;%s);".formatted(vars, (cond == null ? "":" " + cond), (acts == null ? "":" " + acts));
	}
}