package me.hardcoded.visualization;

/**
 * This is a utility interface used by visualization classes to display node structures.
 * 
 * @author HardCoded
 */
public interface Printable {
	/**
	 * Returns the displayed content of this {@code Printable} object.
	 */
	String asString();
	
	/**
	 * Returns the child nodes that this {@code Printable} object contains.
	 */
	Object[] asList();
}
