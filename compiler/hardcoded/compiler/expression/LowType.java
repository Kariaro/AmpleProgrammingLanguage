package hardcoded.compiler.expression;

import hardcoded.compiler.constants.Atom;

public class LowType {
	/**
	 * The type of this {@code AtomField}
	 */
	private final Atom type;
	
	/**
	 * If this atomType is a pointer type this will be its size.
	 * The maximum allowed size is 255 after that it needs to be
	 * casted.
	 */
	private final int depth;
	
	LowType(Atom type, int depth) {
		this.type = type;
		this.depth = depth;
	}
	
	public int hashCode() {
		return depth | (type.ordinal() << 8);
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof LowType)) return false;
		return hashCode() == obj.hashCode();
	}
	
	public Atom type() {
		return type;
	}
	
	public int size() {
		return type.size();
	}
	
	public int depth() {
		return depth;
	}
	
	public boolean isSigned() {
		return type.isSigned();
	}
	
	public boolean isNumber() {
		return type.isNumber();
	}
	
	public boolean isPointer() {
		return depth != 0;
	}
	
	public boolean isFloating() {
		return type.isFloating();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < depth; i++) sb.append('*');
		return type + sb.toString();
	}
	
	public static LowType getPointer(LowType type, int length) {
		return new LowType(type.type, type.depth + length);
	}
	
	public static LowType largest(LowType a, LowType b) {
		return new LowType(Atom.largest(a.type, b.type), 0);
	}
	
	@Deprecated
	public static LowType get(int size, int depth) {
		return new LowType(Atom.get(0, true, false), depth);
	}

	public static LowType create(Atom type) {
		return new LowType(type, 0);
	}
	
	/**
	 * Returns the default pointer size.
	 * @return the default pointer size
	 */
	public static int getPointerSize() {
		return 8;
	}
}
