package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.expression.Expression;
import hardcoded.utils.StringUtils;

public class ExprStat extends Statement {
	public List<Expression> list = new ArrayList<>();
	
	public ExprStat(Expression expr) {
		super(false);
		list.add(expr);
	}
	
	public Expression expr() {
		if(list.isEmpty()) return null;
		return list.get(0);
	}
	
	public String asString() { return toString(); }
	public Object[] asList() { return list.toArray(); }
	public String toString() { return StringUtils.join(" ", list); }
}