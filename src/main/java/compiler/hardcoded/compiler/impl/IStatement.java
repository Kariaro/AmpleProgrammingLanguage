package hardcoded.compiler.impl;

import java.util.List;

/**
 * This interface is a simplified version of the internal statement class.
 * 
 * <p>The internal class is implemented here {@linkplain hardcoded.compiler.statement.Statement}
 * 
 * @author HardCoded
 * @since v0.1
 */
public interface IStatement {
	
	/**
	 * Returns a set of elements inside of this statement or {@code null} if {@link #hasStatements} was {@code false}.
	 * The returned set does not update if the internal list gets modified.
	 * @return a set of elements inside of this statement
	 */
	List<IStatement> getStatements();
	
	/**
	 * Returns {@code true} if this statement has elements.
	 * @return {@code true} if this statement has elements
	 */
	boolean hasStatements();
	
	/**
	 * Returns the line index that this statement was declared on.
	 * @return the line index that this statement was declared on
	 */
	@Deprecated
	default int getLineIndex() {
		// TODO: Implement statement line index methods!
		return -1;
	}
}