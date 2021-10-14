package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.types.HighType;

public class VariableStat extends Statement {
	public List<Expression> list;
	
	public HighType type;
	public String name;
	
	public boolean isArray;
	public int arraySize;
	
	public VariableStat(HighType type) {
		super(false);
		
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

	@Override
	public Object[] asList() {
		if(isArray) return new Object[] { name, arraySize };
		if(!isInitialized()) return new Object[] { name };
		return new Object[] { name, value() };
	}
	
	@Override
	public String asString() {
		return toString();
	}
	
	@Override
	public String toString() {
		if(isArray) return "%s %s[%d]".formatted(type, name, arraySize);
		if(!isInitialized()) return "%s %s;".formatted(type, name);
		return "%s %s = %s;".formatted(type, name, value());
	}
}