package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.types.HighType;

public class Variable implements Statement {
	public List<Expression> list;
	
	public HighType type;
	public String name;
	
	public boolean isArray;
	public int arraySize;
	
	public Variable(HighType type) {
		this.list = new ArrayList<>();
		this.type = type;
	}
	
	public HighType valueType() {
		return type;
	}
	
	public void setValue(Expression expr) {
		if(list.size() < 1) list.add(expr);
		else list.set(0, expr);
	}
	
	public Expression value() {
		if(list.size() < 1) return null;
		return list.get(0);
	}
	
	public boolean isInitialized() {
		return !list.isEmpty();
	}
	
	public boolean hasStatements() { return false; }
	public List<Statement> getStatements() { return null; }
	
	public String asString() { return toString(); }
	public Object[] asList() {
		if(isArray) return new Object[] { name, arraySize };
		if(!isInitialized()) return new Object[] { name };
		return new Object[] { name, value() };
	}
	
	public String toString() {
		if(isArray) return type + " " + name + "[" + arraySize + "];";
		if(!isInitialized()) return type + " " + name + ";";
		return type + " " + name + " = " + value() + ";";
	}
}