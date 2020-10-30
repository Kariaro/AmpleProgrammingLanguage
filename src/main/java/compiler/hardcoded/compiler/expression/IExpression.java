package hardcoded.compiler.expression;

import java.util.Set;

import hardcoded.compiler.constants.ExprType;

/**
 * Access to expressions.
 * 
 * @author HardCoded
 */
public interface IExpression {
	/**
	 * Returns the type of this expression.
	 * @return the type of this expression
	 */
	public ExprType type();

	/**
	 * Returns the size of this expression.
	 * @return the size of this expression
	 */
	public LowType size();
	
	/**
	 * Returns a list of elements inside of this expression or {@code null} if {@link #hasExpressions} was {@code false}.
	 * The returned list does not update if the internal list gets modified.
	 * @return a list of elements inside of this expression
	 */
	public Set<IExpression> getExpressions();
	
	/**
	 * Returns {@code true} if this expression has elements.
	 * @return {@code true} if this expression has elements
	 */
	public boolean hasExpressions();
}
