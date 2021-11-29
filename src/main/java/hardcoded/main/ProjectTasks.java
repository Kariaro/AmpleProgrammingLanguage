package hardcoded.main;

class ProjectTasks {
	/**
	 * TODO: Make it easier to use the code of this compiler as an API for other projects.
	 * 
	 * One key feature is that the ParseTreeGenerator should generate more information
	 * and give a developer access to more context about the source code.
	 * The ParseTreeGenerator should keep information about where a expression or block
	 * is stored and what tokens it uses.
	 * It should also provide information about where errors and warnings are located
	 * inside the project with their messages and file locations still there.
	 */
	
	/**
	 * TODO: Make most of the API classes interfaces and hide the actuall code from the users.
	 * By doing this the interface will be clean and the api used wont have problems using classes.
	 * They will only be able to se the importaint functions and nothing more.
	 * This can also be achieved by wrapping classes but that will remove the easy solution of using the
	 * base implementation.
	 * 
	 * ----------------------------------------------------------------
	 * 
	 * IProgram:
	 *     IFunction[]  : getFunctions()
	 *     ...          : getSyntaxMarkers()
	 *     boolean      : hasErrors()
	 * 
	 * ----------------------------------------------------------------
	 * 
	 * IFunction:
	 *     String       : getName()
	 *     ILowType[]   : getParameters()
	 *     ILowType     : getReturnType()
	 *     IStatement   : getBody()
	 * 
	 * ----------------------------------------------------------------
	 * 
	 * IStatement:
	 *     ...          : getType() <Maybe>
	 *     boolean      : hasStatements()
	 *     IStatement[] : getStatements()
	 * 
	 * ----------------------------------------------------------------
	 * 
	 */
}
