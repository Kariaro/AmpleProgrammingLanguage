package hardcoded.compiler;

// TODO: Implement for all error codes
public enum Error {
	UNINITIALIZED_VARIABLE("The variable '{0}' was uninitialized."),
	FUNCTION_ALREADY_DEFINED("The function '{0}' was already defined."),
	REDECLARATION_OF_LOCAL_VARIABLE("Redeclaration of a local variable '{0}'"),
	INVALID_RETURN_TYPE("Invalid return type. Expected a '{0}' but got '{1}'"),
	INVALID_FUNCTION_NAME("The name '{0}' is not a vaild function name."),
	UNSET_PRIMITIVE("You cannot unset the primitive type '{0}'"),
	REDECLARATION_OF_A_TYPE_NAME("The type '{0}' has already been defined."),
	VARIABLE_NOT_FOUND_IN_CURRENT_SCOPE("The variable '{0}' was not found in the current scope."),
	NOT_ENOUGH_ARGUMENTS("Not enough arguments to call function '{0}'. Expected '{1}' argument but got '{2}'"),
	
	;
	
	public final String message;
	private Error(String value) {
		this.message = value;
	}
	
	public void print() {
		
	}
}
