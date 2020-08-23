package hardcoded.compiler;

import java.util.Objects;

public class Type {
	private final String name;
	private final int size;
	private final boolean floating;
	private final boolean signed;
	
	public Type(String name, int size) {
		this(name, size, false, true);
	}
	
	public Type(String name, int size, boolean floating, boolean signed) {
		this.name = name;
		this.size = size;
		this.floating = floating;
		this.signed = (floating ? true:signed);
	}
	
	public Type type() {
		return this;
	}
	
	public String name() {
		return name;
	}
	
	public boolean isFloating() {
		return floating;
	}
	
	public boolean isSigned() {
		return signed;
	}
	
	public int size() {
		return size;
	}
	
	@Override
	public boolean equals(Object obj) {
		return Objects.equals(this, obj);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
