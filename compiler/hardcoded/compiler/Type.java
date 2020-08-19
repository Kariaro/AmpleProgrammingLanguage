package hardcoded.compiler;

import java.util.Objects;

public class Type {
	private final String name;
	private final int size;
	private final boolean floating;
	
	public Type(String name, int size) {
		this.name = name;
		this.size = size;
		this.floating = false;
	}
	
	public Type(String name, int size, boolean floating) {
		this.name = name;
		this.size = size;
		this.floating = floating;
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
