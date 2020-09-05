package hardcoded.compiler.constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import hardcoded.compiler.Expression.AtomType;
import hardcoded.compiler.types.PrimitiveType;
import hardcoded.compiler.types.Type;

/**
 * This is a container class for all primitive data types.
 * @author HardCoded
 */
public final class Primitives {
	private Primitives() {
		
	}
	
	private static final Set<Type> PRIMITIVE;
	public static final Type VOID = new PrimitiveType("void", null, 0);
	// public static final Type DOUBLE = new PrimitiveType("double", AtomType.int8, 8, true, true);
	// public static final Type FLOAT = new PrimitiveType("float", AtomType.int4, 4, true, true);
	public static final Type LONG = new PrimitiveType("long", AtomType.i64, 8);
	public static final Type INT = new PrimitiveType("int", AtomType.i32, 4);
	public static final Type SHORT = new PrimitiveType("short", AtomType.i16, 2);
	public static final Type BYTE = new PrimitiveType("byte", AtomType.i8, 1);
	public static final Type CHAR = new PrimitiveType("char", AtomType.i8, 1);
	public static final Type BOOL = new PrimitiveType("bool", AtomType.i8, 1);
	
	static {
		Set<Type> types = new HashSet<Type>();
		types.add(VOID);
		// types.add(DOUBLE);
		// types.add(FLOAT);
		types.add(LONG);
		types.add(INT);
		types.add(SHORT);
		types.add(BYTE);
		types.add(CHAR);
		types.add(BOOL);
		
		PRIMITIVE = Collections.unmodifiableSet(types);
	}
	
	/**
	 * Checks if the string is a primitive type.
	 * @param value
	 * @return true if the string was a primitive type.
	 */
	public static boolean contains(String value) {
		for(Type t : PRIMITIVE) {
			if(t.name().equals(value)) return true;
		}
		
		return false;
	}
	
	/**
	 * Get the primitive datatype with a specified name.
	 * @param value
	 * @return the primitive if found otherwise null.
	 */
	public static Type getType(String value) {
		for(Type t : PRIMITIVE) {
			if(t.name().equals(value)) return t;
		}
		
		return null;
	}
	
	public static Type getTypeFromAtom(AtomType type) {
		if(type == AtomType.i64) return LONG;
		if(type == AtomType.i32) return INT;
		if(type == AtomType.i16) return SHORT;
		if(type == AtomType.i8) return BYTE;
		return null;
	}
	
	/**
	 * Returns a unmodifiable set with all primitives inside of it.
	 * @return the set of primitives.
	 */
	public static Set<Type> getAllTypes() {
		return PRIMITIVE;
	}
}