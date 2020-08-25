package hardcoded.errors;

// TODO: Implement for all error codes
public enum Error {
	FUNCTION_ALREADY_DEFINED("The function '{0}' was already defined."),
	INVALID_FUNCTION_NAME("The name '{0}' is not a vaild function name."),
	TOO_MANY_CALL_ARGUMENTS("Too many calling arguments to the function '{0}'"),
	NOT_ENOUGH_ARGUMENTS("Not enough arguments to call function '{0}'. Expected '{1}' argument but got '{2}'"),
	
	@Deprecated
	INVALID_FUNCTION_DECLARATION("This function was already defined as a......"),
	
	UNINITIALIZED_VARIABLE("The variable '{0}' was uninitialized."),
	VARIABLE_NOT_FOUND_IN_CURRENT_SCOPE("The variable '{0}' was not found in the current scope."),
	REDECLARATION_OF_LOCAL_VARIABLE("Redeclaration of a local variable '{0}'"),
	
	INVALID_RETURN_TYPE("Invalid return type. Expected a '{0}' but got '{1}'"),
	REDECLARATION_OF_A_TYPE_NAME("The type '{0}' has already been defined."),
	UNSET_PRIMITIVE("You cannot unset the primitive type '{0}'"),
	
	INVALID_UNARY_EXPRESSION_OPERATION("Invalid use of unary expression. The value you are trying to modify is not modifiable."),
	;
	
	public final String message;
	private Error(String value) {
		this.message = value;
	}
	
	public void print() {
		
	}
}
