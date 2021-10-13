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
			if(override_size.isInvalid()) {
				throw new NullPointerException();
			}
			
			return override_size;
		}
		
		if(type == ExprType.comma) {
			return list.get(list.size() - 1).size();
		}
		
		if(type == ExprType.call) {
			return list.get(0).size();
		}
		
		LowType lowType = super.size();
		if(lowType.isPointer() || lowType.isNumber()) {
			if(type == ExprType.decptr) return lowType.isPointer() ? lowType.nextLowerPointer():LowType.INVALID;
			if(type == ExprType.incptr) return lowType.nextHigherPointer();
		} else {
			return LowType.INVALID;
		}
		
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
		if(type() == ExprType.cast) {
			return "%s(%s, %s)".formatted(type(), StringUtils.join(", ", list), override_size);
		}
		
		return "%s(%s)".formatted(type(), StringUtils.join(", ", list)); // + ":" + size();
	}
}