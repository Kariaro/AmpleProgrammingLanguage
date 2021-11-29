package hardcoded.compiler.impl;

import java.util.List;
import java.util.Set;

import hardcoded.compiler.errors.SyntaxMarker;

/**
 * This interface is a simplified version of the internal program class.
 * 
 * <p>The internal class is implemented here {@linkplain hardcoded.compiler.statement.Program}
 * 
 * @author HardCoded
 */
public interface IProgram {
	/**
	 * Returns a unmodifiable list of functions.
	 */
	List<IFunction> getFunctions();
	
	/**
	 * Returns a unmodifiable set of all imported files.
	 */
	Set<String> getImportedFiles();
	
	/**
	 * Returns a unmodifiable list of syntax markers.
	 */
	List<SyntaxMarker> getSyntaxMarkers();
	
	/**
	 * Returns {@code true} if this program has errors.
	 */
	boolean hasErrors();
}
