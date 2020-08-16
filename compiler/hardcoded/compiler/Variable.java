package hardcoded.compiler;

public class Variable implements Stable {
	public Type type;
	public String name;
	public Expression value;
	public boolean initialized;
	
	public boolean isArray;
	public int arraySize;
	
	public Variable() {
		
	}
	
	public Type type() {
		return type;
	}
	
	@Override
	public String toString() {
		if(isArray) return name + "[" + arraySize + "]";
		if(!initialized) return name;
		return name + " = " + value;
	}
	
	public String listnm() { return "="; }
	public Object[] listme() {
		if(isArray) return new Object[] { name, arraySize };
		return new Object[] { name, value };
	}
}
