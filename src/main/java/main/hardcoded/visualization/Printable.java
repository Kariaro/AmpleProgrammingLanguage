package hardcoded.visualization;

/**
 * This is a utility interface used by visualization to display node structures.
 * 
 * @author HardCoded
 */
public interface Printable {
	/**
	 * Returns the displayed content of this {@code Printable} object.
	 * @return the displayed content of this {@code Printable} object
	 */
	public String asString();
	
	/**
	 * Returns the child nodes that this {@code Printable} object contains.
	 * @return the child nodes that this {@code Printable} object contains
	 */
	public Object[] asList();
}
