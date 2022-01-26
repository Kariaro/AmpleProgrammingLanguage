package me.hardcoded.compiler.impl;

import java.util.List;

/**
 * This interface is a simplified version of the internal statement class.
 * 
 * <p>The internal class is implemented here {@linkplain me.hardcoded.compiler.statement.Statement}
 * 
 * @author HardCoded
 */
public interface IStatement extends ISyntaxLocation {
	/**
	 * Returns a list of elements inside this statement
	 */
	List<IStatement> getStatements();
}