package hardcoded.compiler.constants;

import java.util.*;

/**
 * This is a container class for all modifiers.
 * 
 * @author HardCoded
 */
public final class Modifiers {
	private Modifiers() {}
	
	public static final Modifier EXPORT = new Modifier("export", 1 << 0);
	private static final Modifier[] MODIFIERS = {
		EXPORT
	};
	
	/**
	 * Checks if the string value is a modifier.
	 * @param	value
	 * @return	{@code true} if the string value was a modifier
	 */
	public static boolean contains(String value) {
		for(Modifier modifier : MODIFIERS) {
			if(modifier.name().equals(value)) return true;
		}
		return false;
	}
	
	public static Modifier get(String value) {
		for(Modifier modifier : MODIFIERS) {
			if(modifier.name().equals(value)) return modifier;
		}
		
		return null;
	}
	
	/**
	 * Returns a list with all modifiers inside of it.
	 * @return a list with all modifiers
	 */
	public static List<Modifier> getAllModifiers() {
		return Collections.unmodifiableList(Arrays.asList(MODIFIERS));
	}
	
	public static class Modifier {
		private String name;
		private int mask;
		
		private Modifier(String name) {
			this.name = name;
		}
		
		private Modifier(String name, int mask) {
			this.name = name;
			this.mask = mask;
		}
		
		public int value() {
			return mask;
		}
		
		public String name() {
			return name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}