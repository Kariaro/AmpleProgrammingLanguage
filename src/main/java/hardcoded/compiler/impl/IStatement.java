package hardcoded.compiler.impl;

import java.util.List;

/**
 * This interface is a simplified version of the internal statement class.
 * 
 * <p>The internal class is implemented here {@linkplain hardcoded.compiler.statement.Statement}
 * 
 * @author HardCoded
 */
public interface IStatement extends ISyntaxLocation {
	/**
	 * Returns a list of elements inside of this statement or {@code null} if {@link #hasStatements} was {@code false}.
	 * The returned list does not update if the internal list gets modified.
	 * @return a list of elements inside of this statement
	 */
	List<IStatement> getStatements();
	
	/**
	 * Returns {@code true} if this statement has elements.
	 */
	boolean hasStatements();
}