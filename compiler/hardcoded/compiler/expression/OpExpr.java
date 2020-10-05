package hardcoded.compiler.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hardcoded.compiler.constants.AtomType;
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
	
	public AtomType override_size;
	public AtomType calculateSize() {
		if(type == ExprType.cast) {
			return override_size;
		}
		
		return Expression.super.calculateSize();
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
		return type + "(" + StringUtils.join(", ", list) + ")" + ":" + this.calculateSize();
	}
}