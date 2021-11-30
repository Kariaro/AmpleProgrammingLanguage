package hardcoded.compiler.statement;

import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.impl.ISyntaxPosition;

public class DoWhileStat extends NestedStat {
	public DoWhileStat(Expression condition, Statement body, ISyntaxPosition syntaxPosition) {
		super(2);
		set(0, new ExprStat(condition));
		set(1, body);
		this.syntaxPosition = syntaxPosition;
	}
	
	public Expression getCondition() {
		// TODO: These are never null
		return ((ExprStat)get(0)).expr();
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
		return "do { ... } while(%s)".formatted(getCondition());
	}
}