package me.hardcoded.compiler.statement;

import java.util.Objects;

import me.hardcoded.compiler.expression.Expression;
import me.hardcoded.compiler.impl.ISyntaxPosition;

public class ForStat extends NestedStat {
	public ForStat(Statement variables, Expression condition, Expression action, Statement body, ISyntaxPosition syntaxPosition) {
		super(4);
		set(0, variables);
		set(1, new ExprStat(condition));
		set(2, new ExprStat(action));
		set(3, body);
		this.syntaxPosition = syntaxPosition;
	}
	
	public Statement getVariables() {
		return get(0);
	}
	
	public Expression getCondition() {
		return ((ExprStat)get(1)).expr();
	}
	
	public Expression getAction() {
		return ((ExprStat)get(2)).expr();
	}
	
	public Statement getBody() {
		return get(3);
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
		
		return "for(%s;%s;%s)".formatted(vars, (cond == null ? "":" " + cond), (acts == null ? "":" " + acts));
	}
}