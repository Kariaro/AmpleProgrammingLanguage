package hardcoded.compiler.expression;

import java.util.Arrays;

import hardcoded.compiler.constants.ExprType;
import hardcoded.utils.StringUtils;

public class OpExpr extends Expression {
	public OpExpr(ExprType type, Expression... array) {
		super(type, true);
		list.addAll(Arrays.asList(array));
	}
	
	/**
	 * Will only be used if this Expression is a cast expression.
	 */
	public LowType override_size;
	public LowType size() {
		ExprType type = type();
		if(type == ExprType.cast) {
			return override_size;
		}
		
		LowType lowType = super.size();
		if(type == ExprType.decptr) return lowType.nextLowerPointer();
		if(type == ExprType.addptr) return lowType.nextHigherPointer();
		
		return lowType;
	}
	
	public Expression clone() {
		OpExpr expr = new OpExpr(type());
		expr.override_size = override_size;
		for(Expression e : list) {
			// FIXME: Values inside the elements list should never be null!
			if(e == null) continue;
			expr.add(e.clone());
		}
		
		return expr;
	}
	
	public String asString() { return type().toString(); }
	public String toString() {
		return type() + "(" + StringUtils.join(", ", list) + ")" + ":" + size();
	}
}