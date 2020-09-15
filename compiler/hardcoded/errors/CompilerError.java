package hardcoded.errors;

public enum CompilerError {
//	FUNCTION_ALREADY_DEFINED("The function '{0}' was already defined."),
//	TOO_MANY_CALL_ARGUMENTS("Too many calling arguments to the function '{0}'"),
//	NOT_ENOUGH_ARGUMENTS("Not enough arguments to call function '{0}'. Expected '{1}' argument but got '{2}'"),
//	
//	UNINITIALIZED_VARIABLE("The variable '{0}' was uninitialized."),
//	VARIABLE_NOT_FOUND_IN_CURRENT_SCOPE("The variable '{0}' was not found in the current scope."),
//	REDECLARATION_OF_LOCAL_VARIABLE("Redeclaration of a local variable '{0}'"),
//	
//	INVALID_RETURN_TYPE("Invalid return type. Expected a '{0}' but got '{1}'"),
//	REDECLARATION_OF_A_TYPE_NAME("The type '{0}' has already been defined."),
//	UNSET_PRIMITIVE("You cannot unset the primitive type '{0}'"),
	
	
	INVALID_UNARY_EXPRESSION_OPERATION("Invalid use of a unary expression. The value you are trying to modify is not modifiable."),
	INVALID_MODIFICATION("The value you are trying to modify is not modifiable."),
	
	INVALID_CHAR_LITERAL_SIZE("Invalid character constant."),
	INVALID_VARIABLE_NAME("Invalid variable name. The name '%s' is not a valid variable name."),
	INVALID_FUNCTION_NAME("Invalid function name. The name '%s' is not a valid function name."),
	INVALID_CAST_TYPE("Invalid cast type. You cannot cast into a '%s'."),
	INVALID_TYPE("Invalid type '%s'."),
	
	MISSING_FUNCTION_PARAMETER_SEPARATOR("Invalid function parameter separator. Expected a comma ',' but got '%s'"),
	
	
	UNCLOSED_ARRAY_DEFINITION("Unclosed array definition. Expected the character ']' but got '%s'"),
	UNCLOSED_ARRAY_EXPRESSION("Unclosed brackets. Expected the character ']' but got '%s'"),
	
	UNCLOSED_CURLY_BRACKETS_STATEMENT("Unclosed curly brackets. Expected the character '}' but got '%s'"),

	INVALID_XXX_DEFINITION_EXPECTED_OPEN_PARENTHESIS("Invalid %s definition. Expected open parenthesis '(' but got '%s'"),
	INVALID_XXX_STATEMENT_EXPECTED_OPEN_PARENTHESIS("Invalid %s statement. Expected open parenthesis '(' but got '%s'"),
	INVALID_XXX_STATEMENT_EXPECTED_SEMICOLON("Invalid %s statement. Expected a semicolon ';' but got '%s'"),
	
	UNCLOSED_EXPRESSION_PARENTHESES("Unclosed parentheses. Expected the character ')' but got '%s'"),
	UNCLOSED_STATEMENT_PARENTHESES("Unclosed parentheses. Expected the character ')' but got '%s'"),
	UNCLOSED_CALL_PARENTHESES("Unclosed call parentheses. Expected the character ')' but got '%s'"),
	UNCLOSED_CAST_PARENTHESES("Unclosed cast parentheses. Expected the character ')' but got '%s'"),
	
	UNCLOSED_VARIABLE_DECLARATION("Unclosed variable declaration. Did you forget a semicolon here?"),
	
	;
	
	public final String message;
	private CompilerError(String value) {
		this.message = value;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void print() {
		
	}
}
