package me.hardcoded.compiler.expression;

import java.util.Objects;

import me.hardcoded.compiler.constants.Atom;
import me.hardcoded.compiler.errors.CompilerException;

public class LowType {
	public static final LowType INVALID = new LowType(Atom.unf, 0);
	
	private final Atom type;
	private final int depth;
	
	private LowType(Atom type, int depth) {
		if(depth < 0 || depth > 255) {
			throw new CompilerException("LowType pointer depth was outside bounds. (0 < depth < 256). Got '%s'".formatted(depth));
		}
		
		this.type = Objects.requireNonNull(type, "LowType type must not be null");
		this.depth = depth;
	}

	@Override
	public int hashCode() {
		return depth | (type.ordinal() << 8);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof LowType)
			&& hashCode() == obj.hashCode();
	}
	
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
	 */
	public int baseSize() {
		if(depth > 1) return getPointerSize();
		// TODO: If the depth is 0 then this should return a invalid value !!
		return type.size();
	}

	@Override
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
	 */
	public static int getPointerSize() {
		return getPointerType().size();
	}
	
	/**
	 * Returns the default pointer type.
	 */
	public static Atom getPointerType() {
		// TODO: This should be specified by the compiler.
		return Atom.i64;
	}
	
	/**
	 * Returns {@code true} if this type is invalid.
	 */
	public boolean isInvalid() {
		return this == INVALID;
	}
}
