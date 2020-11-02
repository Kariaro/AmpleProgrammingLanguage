package hardcoded.compiler.errors;

import hardcoded.compiler.impl.ILocation;

public interface SyntaxMarker extends ILocation {
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int INFO = 3;
	
	public CompilerError getCompilerError();
	
	/**
	 * Returns the message of this marker.
	 * @return the message of this marker
	 */
	public String getMessage();
	
	/**
	 * Returns the internal compilers thrown string.
	 * @return the internal compilers thrown string
	 */
	public String getCompilerMessage();
	
	/**
	 * Returns the severity of the marker.
	 * @return the severity of the marker
	 */
	public int getSeverity();
}
