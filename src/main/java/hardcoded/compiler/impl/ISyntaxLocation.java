package hardcoded.compiler.impl;

/**
 * @author HardCoded
 */
public interface ISyntaxLocation {
	/**
	 * Returns the syntax position that this block was declared on.
	 */
	ISyntaxPosition getSyntaxPosition();
}
