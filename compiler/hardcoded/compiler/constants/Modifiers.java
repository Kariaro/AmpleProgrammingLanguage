package hardcoded.compiler.constants;

import java.util.*;

/**
 * This is a container class for all modifier keywords.
 * @author HardCoded
 */
public final class Modifiers {
	private static final Set<String> MODIFIERS;
	private Modifiers() {
		
	}

	static {
		Set<String> modifiers = new HashSet<>();
		modifiers.addAll(Arrays.asList(
			"export"
		));
		
		MODIFIERS = Collections.unmodifiableSet(modifiers);
	}
	
	/**
	 * Checks if the string value is a modifier.
	 * @param value
	 * @return true if the string value was a modifier.
	 */
	public static boolean contains(String value) {
		return MODIFIERS.contains(value);
	}
	
	/**
	 * Returns a list with all modifiers inside of it.
	 * @return A set of modifiers.
	 */
	public static Set<String> getAllModifiers() {
		return MODIFIERS;
	}
}
