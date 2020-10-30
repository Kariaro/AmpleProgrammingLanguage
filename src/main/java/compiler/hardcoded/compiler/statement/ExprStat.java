package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.expression.Expression;
import hardcoded.utils.StringUtils;

public class ExprStat extends Statement {
	public List<Expression> list = new ArrayList<>();
	
	public ExprStat(Expression expr) {
		list.add(expr);
	}
	
	public Expression expr() {
		if(list.isEmpty()) return null;
		return (Expression)list.get(0);
	}
	
	public boolean hasStatements() { return false; }
	public List<Statement> getStatements() { return null; }
	
	public String asString() { return toString(); }
	public Object[] asList() { return list.toArray(); }
	public String toString() { return StringUtils.join(" ", list); }
}