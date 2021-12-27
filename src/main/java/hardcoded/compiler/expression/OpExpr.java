package hardcoded.compiler.expression;

import java.util.Arrays;

import hardcoded.utils.StringUtils;

public class OpExpr extends Expression {
	/**
	 * Will only be used if this Expression is a cast expression.
	 */
	public LowType override_size;
	
	public OpExpr(ExprType type, Expression... array) {
		super(type, true);
		getElements().addAll(Arrays.asList(array));
	}
	
	@Override
	public LowType size() {
		ExprType type = type();
		if(type == ExprType.cast) {
			if(override_size.isInvalid()) {
				throw new NullPointerException();
			}
			
			return override_size;
		}
		
		if(type == ExprType.comma) {
			return getElements().get(getElements().size() - 1).size();
		}
		
		if(type == ExprType.call) {
			return getElements().get(0).size();
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

	@Override
	public Expression clone() {
		OpExpr expr = new OpExpr(type());
		expr.override_size = override_size;
		for(Expression e : getElements()) {
			expr.add(e.clone());
		}
		
		return expr;
	}

	@Override
	public String asString() {
		return type().toString();
	}
	
	@Override
	public String toString() {
		if(type() == ExprType.cast) {
			return "%s(%s, %s)".formatted(type(), StringUtils.join(", ", getElements()), override_size);
		}
		
		return "%s(%s)".formatted(type(), StringUtils.join(", ", getElements()));
	}
}