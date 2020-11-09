package hardcoded.compiler.errors;

import hardcoded.compiler.impl.ILocation;

public interface SyntaxMarker extends ILocation {
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int INFO = 3;
	
	CompilerError getCompilerError();
	
	/**
	 * Returns the message of this marker.
	 * @return the message of this marker
	 */
	String getMessage();
	
	/**
	 * Returns the internal compilers thrown string.
	 * @return the internal compilers thrown string
	 */
	String getCompilerMessage();
	
	/**
	 * Returns the severity of the marker.
	 * @return the severity of the marker
	 */
	int getSeverity();
}
