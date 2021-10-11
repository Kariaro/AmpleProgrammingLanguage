package hardcoded.compiler.impl;

import java.io.File;

/**
 * This interface contains information about where a object is located inside a source file.
 * 
 * @author HardCoded
 * @since v0.1
 */
public interface ILocation {
	/**
	 * The default return value for a invalid index or file offset. {@value}
	 */
	public static final int INVALID = -1;
	
	/**
	 * Returns the file that this object was declared in.
	 * @return the file that this object was declared in
	 */
	File getDeclaringFile();
	
	/**
	 * Returns the file offset that this object was declared on.
	 * @return the file offset that this object was declared on
	 */
	default int getFileOffset() {
		return INVALID;
	}
	
	/**
	 * Returns the line index that this object was declared on.
	 * @return the line index that this object was declared on
	 */
	default int getLineIndex() {
		return INVALID;
	}
	
	/**
	 * Returns the column index that this object was declared on.
	 * @return the column index that this object was declared on
	 */
	default int getColumnIndex() {
		return INVALID;
	}
	
	/**
	 * Returns the length of the object.
	 * @return the length of the object
	 */
	default int getLength() {
		return INVALID;
	}
}
