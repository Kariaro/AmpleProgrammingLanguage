package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.types.HighType;

public class VariableStat extends Statement {
	public List<Expression> list;
	
	private final String name;
	// TODO: Make this field final.
	private HighType type;
	
	// TODO: Make these variables private.
	public boolean isArray;
	public int arraySize;
	
	public VariableStat(HighType type, String name) {
		super(false);
		
		this.list = new ArrayList<>();
		this.name = name;
		this.type = type;
	}
	
	/**
	 * Returns the name of this variable statement.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the type of this variable statement.
	 */
	public HighType getType() {
		return type;
	}
	
	@Deprecated
	public void setType(HighType type) {
		this.type = type;
	}
	
	public void setValue(Expression expr) {
		if(list.size() < 1) {
			list.add(expr);
		} else {
			list.set(0, expr);
		}
	}
	
	public Expression getValue() {
		return list.isEmpty() ? null:list.get(0);
	}
	
	public boolean isInitialized() {
		return !list.isEmpty();
	}

	@Override
	public Object[] asList() {
		if(isArray) return new Object[] { name, arraySize };
		if(!isInitialized()) return new Object[] { name };
		return new Object[] { name, getValue() };
	}
	
	@Override
	public String asString() {
		return toString();
	}
	
	@Override
	public String toString() {
		if(isArray) return "%s %s[%d]".formatted(type, name, arraySize);
		if(!isInitialized()) return "%s %s".formatted(type, name);
		return "%s %s = %s".formatted(type, name, getValue());
	}
}