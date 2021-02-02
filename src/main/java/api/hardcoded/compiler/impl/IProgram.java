package hardcoded.compiler.impl;

import java.util.List;

import hardcoded.compiler.errors.SyntaxMarker;

/**
 * This interface is a simplified version of the internal program class.
 * 
 * <p>The internal class is implemented here {@linkplain hardcoded.compiler.Program}
 * 
 * @author HardCoded
 * @since v0.2
 */
public interface IProgram {
	/**
	 * Returns a unmodifiable list of functions.
	 * @return a unmodifiable list of functions
	 */
	List<IFunction> getFunctions();
	
	/**
	 * Returns a unmodifiable list of imported files.
	 * @return a unmodifiable list of imported files
	 */
	List<String> getImportedFiles();
	
	/**
	 * Returns a unmodifiable list of syntax markers.
	 * @return a unmodifiable list of syntax markers
	 */
	List<SyntaxMarker> getSyntaxMarkers();
	
	/**
	 * Returns {@code true} if this program has errors.
	 * @return {@code true} if this program has errors
	 */
	boolean hasErrors();
}
