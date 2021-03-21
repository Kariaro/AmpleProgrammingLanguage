package com.hardcoded.compiler.impl.context;

/**
 * A reference value to a function, variable or temporary value
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class Reference {
	protected final String name;
	protected final int temp_index;
	
	private Reference(int temp_index) {
		this.name = "#temp_" + temp_index;
		this.temp_index = temp_index;
	}
	
	private Reference(String name) {
		this.name = name;
		this.temp_index = -1;
	}
	
	public boolean isTemporary() {
		return temp_index != -1;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static Reference get(String name) {
		return new Reference(name);
	}
	
	public static Reference get(int temp_index) {
		return new Reference(temp_index);
	}
}
