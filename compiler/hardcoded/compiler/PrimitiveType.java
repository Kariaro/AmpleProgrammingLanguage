package hardcoded.compiler;

public class PrimitiveType extends Type {
	public PrimitiveType(String name, int size, boolean floating) {
		super(name, size, floating);
	}
	
	public PrimitiveType(String name, int size) {
		super(name, size);
	}
}
