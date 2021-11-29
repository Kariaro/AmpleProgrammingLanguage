package hardcoded.compiler.impl;

import java.io.File;

/**
 * This interface is used to specify program blocks
 * such as functions, classes and data structures.
 * 
 * @author HardCoded
 */
public interface IBlock extends ISyntaxLocation {
	public static final IBlock EMPTY = new IBlock() {
		@Override
		public File getDeclaringFile() {
			return null;
		}
		
		@Override
		public ISyntaxPosition getSyntaxPosition() {
			return ISyntaxPosition.empty();
		}
	};

	/**
	 * Returns the file that this block was declared in.
	 */
	File getDeclaringFile();
}
