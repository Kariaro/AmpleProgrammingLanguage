package hardcoded.compiler.impl;

import java.util.List;

import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.types.HighType;

/**
 * This interface is a simplified version of the internal function class.
 * 
 * <p>The internal class is implemented here {@linkplain hardcoded.compiler.statement.compiler.Function}
 * 
 * @author HardCoded
 */
public interface IFunction extends IBlock, IStatement {
	/**
	 * Returns the name of this function.
	 */
	String getName();
	
	/**
	 * Returns the return type of this function.
	 */
	HighType getReturnType();
	
	/**
	 * Returns a unmodifiable list of modifiers.
	 */
	List<Modifier> getModifiers();
	
	/**
	 * Returns a list of statements inside this function.
	 */
	List<IStatement> getStatements();
}
