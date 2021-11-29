package hardcoded.compiler.errors;

import hardcoded.compiler.impl.ISyntaxLocation;

public interface SyntaxMarker extends ISyntaxLocation {
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int INFO = 3;
	
	/**
	 * Returns the compiler error.
	 */
	CompilerError getCompilerError();
	
	/**
	 * Returns the message of this marker.
	 */
	String getMessage();
	
	/**
	 * Returns the internal compilers thrown string.
	 */
	String getCompilerMessage();
	
	/**
	 * Returns the severity of the marker.
	 */
	int getSeverity();
}
