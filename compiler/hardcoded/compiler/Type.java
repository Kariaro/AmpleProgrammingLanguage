package hardcoded.compiler;

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
	public String toString() {
		return name;
	}
}
