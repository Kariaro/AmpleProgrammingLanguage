package hardcoded.compiler.impl;

import java.util.List;

import hardcoded.compiler.constants.ExprType;
import hardcoded.compiler.expression.LowType;

/**
 * This interface is a simplified version of the internal expression class.
 * 
 * <p>The internal class is implemented here {@linkplain hardcoded.compiler.expression.Expression}
 * 
 * @author HardCoded
 */
public interface IExpression extends ISyntaxLocation {
	/**
	 * Returns the type of this expression.
	 */
	ExprType type();

	/**
	 * Returns the size of this expression.
	 */
	LowType size();
	
	/**
	 * Returns a list of elements inside of this expression or {@code null} if {@link #hasExpressions} was {@code false}.
	 * The returned list does not update if the internal list gets modified.
	 * @return a list of elements inside of this expression
	 */
	List<IExpression> getExpressions();
	
	/**
	 * Returns {@code true} if this expression has elements.
	 */
	boolean hasExpressions();
}
