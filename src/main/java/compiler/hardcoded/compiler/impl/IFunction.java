package hardcoded.compiler.impl;

import java.util.Set;

import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.types.HighType;

/**
 * This interface is a simplified version of the internal function class.
 * 
 * <p>The internal class is implemented here {@linkplain hardcoded.compiler.compiler.Function}
 * 
 * @author HardCoded
 * @since v0.1
 */
public interface IFunction extends IBlock, IStatement, ILocation {
	/**
	 * Returns the name of this function.
	 * @return the name of this function
	 */
	String getName();
	
	/**
	 * Returns the return type of this function.
	 * @return the return type of this function
	 */
	HighType getReturnType();
	
	/**
	 * Returns a unmodifiable set of modifiers.
	 * @return a unmodifiable set of modifiers
	 */
	Set<Modifier> getModifiers();
	
	/**
	 * Returns a unmodifiable set of statements inside of this function.
	 * @return a unmodifiable set of statements inside of this function
	 */
	Set<IStatement> getStatements();
	
	/**
	 * Always returns {@code true}.
	 * @return {@code true}
	 * @hidden
	 */
	default boolean hasStatements() { return true; }

	@Override
	default int getLineIndex() {
		return 0;
	}
}
