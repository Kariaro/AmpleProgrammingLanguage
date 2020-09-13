package hardcoded.compiler.constants;

/**
 * This is a utility interface used by some visualization
 * classes to display a node structure.
 * 
 * @author HardCoded
 */
public interface Printable {
	/** @return The displayed content of this <code>Printable</code> object */
	public String asString();
	
	/** @return The child nodes that this <code>Printable</code> object contains */
	public Object[] asList();
}
