package hardcoded.compiler.expression;

import java.io.File;

import hardcoded.compiler.types.HighType;

/**
 * @author HardCoded
 */
public interface IFunction {
	/**
	 * Returns the name of this function.
	 * @return the name of this function
	 */
	public String getName();
	
	/**
	 * Returns the return type of this function.
	 * @return the return type of this function
	 */
	public HighType getReturnType();
	
	/**
	 * Returns the file that declared this function.
	 * @return the file that declared this function
	 */
	public File getDeclaringFile();
	
	/**
	 * Returns the line index that this function was declared.
	 * @return the line index that this function was declared
	 */
	public int getLineIndex();
	
	/**
	 * Returns the body of this function.
	 * @return the body of this function
	 */
	@SuppressWarnings("deprecation")
	public IStatement getBody();
}
