package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

public class Variable implements Statement {
	public List<Expression> list;
	
	public Type type;
	public String name;
	
	public boolean isArray;
	public int arraySize;
	
	public Variable(Type type) {
		this.list = new ArrayList<>();
		this.type = type;
	}
	
	public Type valueType() {
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
	
	@Override
	public String toString() {
		if(isArray) return type + " " + name + "[" + arraySize + "];";
		if(!isInitialized()) return type + " " + name + ";";
		return type + " " + name + " = " + value() + ";";
	}
	
	public String listnm() { return toString(); }
	public Object[] listme() {
		if(isArray) return new Object[] { name, arraySize };
		if(!isInitialized()) return new Object[] { name };
		return new Object[] { name, value() };
	}
}
