package hardcoded.compiler.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hardcoded.compiler.constants.ExprType;
import hardcoded.utils.StringUtils;

public class OpExpr implements Expression {
	public List<Expression> list;
	public ExprType type;
	
	public OpExpr(ExprType type, Expression... array) {
		this.list = new ArrayList<>(Arrays.asList(array));
		this.type = type;
	}
	
	public OpExpr add(Expression expr) {
		list.add(expr);
		return this;
	}
	
	public void set(int index, Expression expr) {
		list.set(index, expr);
	}
	
	public Expression get(int index) {
		return list.get(index);
	}
	
	public int length() {
		return list.size();
	}
	
	/**
	 * Will only be used if this Expression is a cast expression.
	 */
	public LowType override_size;
	public LowType size() {
		if(type == ExprType.cast) {
			return override_size;
		}
		
		LowType lowType = Expression.super.size();
		if(type == ExprType.decptr) return lowType.nextLowerPointer();
		if(type == ExprType.addptr) return lowType.nextHigherPointer();
		
		return lowType;
	}
	
	public OpExpr clone() {
		OpExpr expr = new OpExpr(type);
		expr.override_size = override_size;
		for(Expression e : list) expr.add(e.clone());
		return expr;
	}
	
	public ExprType type() { return type; }
	public boolean hasElements() { return true; }
	public void remove(int index) {}
	public List<Expression> getElements() { return list; }
	
	public String asString() { return type.toString(); }
	public Object[] asList() { return list.toArray(); }
	
	public String toString() {
		return type + "(" + StringUtils.join(", ", list) + ")" + ":" + this.size();
	}
}