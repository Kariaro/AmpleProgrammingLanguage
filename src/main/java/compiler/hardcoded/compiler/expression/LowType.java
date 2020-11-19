package hardcoded.compiler.expression;

import java.util.Objects;

import hardcoded.compiler.constants.Atom;

public class LowType {
	public static final LowType INVALID = new LowType(Atom.unf, 0);
	
	/**
	 * The type of this {@code LowType}
	 */
	private final Atom type;
	
	/**
	 * The size of this pointer.
	 * The maximum allowed size is 255.
	 */
	private final int depth;
	
	LowType(Atom type, int depth) {
		Objects.requireNonNull(type, "LowType type must not be null"); // Type can never be null.
		if(depth < 0 || depth > 255) throw new AssertionError("LowType pointer depth was outside bounds. (0 < depth < 256). Got '" + depth + "'");
		
		this.type = type;
		this.depth = depth;
	}
	
	public int hashCode() {
		return depth | (type.ordinal() << 8);
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Atom) return depth == 0 && type == obj;
		if(!(obj instanceof LowType)) return false;
		return hashCode() == obj.hashCode();
	}
	
	// TODO: LowType - Should we return the converted atom type?
	public Atom type() {
		return type;
	}
	
	public int size() {
		if(depth != 0) return getPointerSize();
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
	
	public LowType nextLowerPointer() {
		if(this == INVALID) return INVALID;
		return new LowType(type, depth - 1);
	}
	
	public LowType nextHigherPointer() {
		if(this == INVALID) return INVALID;
		return new LowType(type, depth + 1);
	}
	
	/**
	 * Returns the size of the base type if this where a pointer.
	 * The size of the data with one lower pointer.
	 * 
	 * @return
	 */
	public int baseSize() {
		if(depth > 1) return getPointerSize();
		// TODO: If the depth is 0 then this should return a invalid value !!
		return type.size();
	}
	
	public String toString() {
		return type + "*".repeat(depth);
	}
	
	public static LowType getPointer(LowType type, int length) {
		return new LowType(type.type, type.depth + length);
	}
	
	public static LowType largest(LowType a, LowType b) {
		if(a.isPointer()) return a;
		if(b.isPointer()) return b;
		return new LowType(Atom.largest(a.type, b.type), 0);
	}
	
	public static LowType create(Atom type) {
		return new LowType(type, 0);
	}
	
	public static LowType create(Atom type, int depth) {
		return new LowType(type, depth);
	}
	
	
	/**
	 * Returns the default pointer size.
	 * @return the default pointer size
	 */
	public static int getPointerSize() {
		return 8;
	}
	
	/**
	 * Returns the default pointer type.
	 * @return the default pointer type
	 */
	public static Atom getPointerType() {
		return Atom.i64;
	}
	
	/**
	 * Returns {@code true} if this type is invalid.
	 * @return {@code true} if this type is invalid
	 */
	public boolean isInvalid() {
		return this == INVALID;
	}
}
