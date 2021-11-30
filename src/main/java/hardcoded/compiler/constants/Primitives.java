package hardcoded.compiler.constants;

import java.util.*;

import hardcoded.compiler.types.HighType;
import hardcoded.compiler.types.PrimitiveType;

/**
 * This is a container class for all primitive data types.
 * 
 * @author HardCoded
 */
public final class Primitives {
	private static final Map<String, HighType> PRIMITIVES;
	
	public static final HighType VOID = new PrimitiveType("void", Atom.unf);
	public static final HighType LONG = new PrimitiveType("long", Atom.i64);
	public static final HighType INT = new PrimitiveType("int", Atom.i32);
	public static final HighType SHORT = new PrimitiveType("short", Atom.i16);
	public static final HighType BYTE = new PrimitiveType("byte", Atom.i8);
	public static final HighType CHAR = new PrimitiveType("char", Atom.i8);
	public static final HighType BOOL = new PrimitiveType("bool", Atom.i8);
	
	static {
		PRIMITIVES = Map.of(
			VOID.name(),	VOID,
			LONG.name(),	LONG,
			INT.name(),		INT,
			SHORT.name(),	SHORT,
			BYTE.name(),	BYTE,
			CHAR.name(),	CHAR,
			BOOL.name(),	BOOL
		);
	}
	
	/**
	 * Checks if the string is a primitive type.
	 * @param	value
	 * @return	{@code true} if the string was a primitive type
	 */
	public static boolean contains(String value) {
		return PRIMITIVES.containsKey(value);
	}
	
	/**
	 * Get the primitive datatype with a specified name.
	 * @param	value
	 * @return	the primitive if found otherwise {@code null}
	 */
	public static HighType getType(String value) {
		return PRIMITIVES.get(value);
	}
	
	/**
	 * Returns a unmodifiable set with all primitives inside of it.
	 */
	public static Set<HighType> getAllTypes() {
		return Set.copyOf(PRIMITIVES.values());
	}
}
