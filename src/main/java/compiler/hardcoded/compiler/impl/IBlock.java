package hardcoded.compiler.impl;

import java.io.File;

/**
 * This interface is a simplified specifies all program blocks
 * such as functions, classes and data structures.
 * 
 * @author HardCoded
 * @since v0.1
 */
public interface IBlock {
	public static final IBlock EMPTY = new IBlock() {
		public File getDeclaringFile() { return null; }
		public int getLineIndex() { return -1; }
	};

	/**
	 * Returns the file that this block was declared in.
	 * @return the file that this block was declared in
	 */
	File getDeclaringFile();
	
	/**
	 * Returns the line inedx that this block was declared on.
	 * @return the line inedx that this block was declared on
	 */
	int getLineIndex();
}
