package hardcoded.compiler.errors;

import java.io.File;

public interface SyntaxMarker {
	public CompilerError getType();
	
	/**
	 * Returns the message of this marker.
	 * @return the message of this marker
	 */
	public String getMessage();
	
	/**
	 * Returns the column index of this marker.
	 * @return the column index of this marker
	 */
	public int getColumn();
	
	/**
	 * Returns the line index of this marker.
	 * @return the line index of this marker
	 */
	public int getLine();
	

	/**
	 * Returns the source file this marker is located.
	 * @return the source file that marker is located
	 */
	public File getSourceFile();
}
