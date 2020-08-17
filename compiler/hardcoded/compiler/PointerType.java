package hardcoded.compiler;

public class PointerType extends Type {
	private Type type;
	public int pointerLength;

	public static final int POINTER_SIZE = 8;
	
	public PointerType(Type type, int pointerLength) {
		super(null, POINTER_SIZE);
		this.pointerLength = pointerLength;
		this.type = type;
	}
	
	public String name() {
		return type.name();
	}
	
	public Type type() {
		return type;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < pointerLength; i++) sb.append("*");
		return type + sb.toString();
	}
}
