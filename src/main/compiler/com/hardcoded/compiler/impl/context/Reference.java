package com.hardcoded.compiler.impl.context;

/**
 * A reference value to a function, variable or temporary value
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class Reference {
	// private static final AtomicInteger counter = new AtomicInteger();
	public enum Type {
		FUN,
		VAR
	}
	
	protected final String name;
	protected final Type type;
	protected final int temp_index;
	protected final int unique_index;
	// protected final int temp = counter.getAndIncrement();
	
	private Reference(int temp_index) {
		this.name = "#temp_" + temp_index;
		this.type = Type.VAR;
		this.temp_index = temp_index;
		this.unique_index = -1;
	}
	
	private Reference(String name) {
		this.name = name;
		this.type = Type.VAR;
		this.temp_index = -1;
		this.unique_index = -1;
	}
	
	private Reference(String name, int unique_index) {
		this.name = name;
		this.type = Type.VAR;
		this.temp_index = -1;
		this.unique_index = unique_index;
	}
	
	private Reference(String name, Type type, int temp_index, int unique_index) {
		this.name = name;
		this.type = type;
		this.temp_index = temp_index;
		this.unique_index = unique_index;
	}
	
	public Reference clone() {
		return new Reference(name, type, temp_index, unique_index);
	}
	
	public boolean isTemporary() {
		return temp_index != -1;
	}
	
	public boolean isUnique() {
		return unique_index != -1;
	}
	
	public String getName() {
		return name;
	}
	
	public int getUniqueIndex() {
		return unique_index;
	}
	
	public Type getType() {
		return type;
	}
	
	public Reference as(Type type) {
		return new Reference(name, type, temp_index, unique_index);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Reference)) return false;
		Reference that = (Reference)obj;
		
		return this.name.equals(that.name)
			&& this.unique_index == that.unique_index
			&& this.temp_index == that.temp_index;
	}
	
	@Override
	public String toString() {
		// if(unique_index != -1) return String.format("[REG:%02x]", unique_index);
		// if(unique_index == -1) return String.format("(%s:%s)", type, name);
		return String.format("(%s:%s:%d)", type, name, unique_index);
	}
	
	public static Reference get(String name) {
		return new Reference(name);
	}
	
	public static Reference get(int temp_index) {
		return new Reference(temp_index);
	}
	
	public static Reference unique(String name, int unique) {
		return new Reference(name, unique);
	}
	
	public static Reference unique(String name, Type type, int unique) {
		return new Reference(name, type, -1, unique);
	}
}
