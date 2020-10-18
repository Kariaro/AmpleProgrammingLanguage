package hardcoded.compiler.errors;

public enum CompilerError {
	INVALID_UNARY_EXPRESSION_OPERATION("Invalid use of a unary expression. The value you are trying to modify is not modifiable."),
	INVALID_MODIFICATION("The value you are trying to modify is not modifiable."),
	INVALID_CHAR_LITERAL_SIZE("Invalid character constant."),
	INVALID_VARIABLE_NAME("Invalid variable name. The name '%s' is not a valid variable name."),
	INVALID_FUNCTION_NAME("Invalid function name. The name '%s' is not a valid function name."),
	INVALID_FUNCTION_PARAMETER_NAME("Invalid function parameter name '%s'."),
	INVALID_CAST_TYPE("Invalid cast type. You cannot cast into a '%s'."),
	INVALID_TYPE("Invalid type '%s'."),
	INVALID_XXX_EXPECTED_OPEN_PARENTHESIS("Invalid %s. Expected open parenthesis '(' but got '%s'"),
	INVALID_XXX_EXPECTED_SEMICOLON("Invalid %s. Expected a semicolon ';' but got '%s'"),
	INVALID_FUNCTION_CALL_PARAMETER("Invalid function call parameter."),
	INVALID_FUNCTION_CALL_EXPRESSION("Invalid function call expression '%s'."),
	INVALID_FUNCTION_DECLARATION_EXPECTED_XXX("Invalid function declaration. %s."),
	
	INVALID_TERNARY_OPERATOR_MISSING_COLON("Invalid ternary operation a ? b : c. Did you forget a colon here? '%s'"),
	INVALID_VARIABLE_DECLARATION_MISSING_COLON_OR_SEMICOLON("Invalid variable definition. Expected a comma or semicolon but got '%s'"),
	INVALID_ARRAY_VARIABLE_DECLARATION_EXPECTED_INTEGER("Invalid array variable definition. Expected an integer expression but got '%s'"),
	
	TOO_MANY_FUNCTION_CALL_ARGUMENTS("Too many arguments calling function '%s' expected %s"),
	NOT_ENOUGH_FUNCTION_CALL_ARGUMENTS("Not enough arguments to call function '%s' expected %s but got %d"),
	
	MISSING_FUNCTION_PARAMETER_SEPARATOR("Invalid function parameter separator. Expected a comma ',' but got '%s'"),
	
	REDECLARATION_OF_FUNCTION_PARAMETER("Redeclaration of function parameter '%s'."),
	REDECLARATION_OF_LOCAL_VARIABLE("Redeclaration of a local variable '%s'"),
	
	UNDECLARED_VARIABLE_OR_FUNCTION("Could not find the variable or function '%s'"),
	
	UNCLOSED_ARRAY_DEFINITION("Unclosed array definition. Expected the character ']' but got '%s'"),
	UNCLOSED_ARRAY_EXPRESSION("Unclosed brackets. Expected the character ']' but got '%s'"),
	UNCLOSED_CURLY_BRACKETS_STATEMENT("Unclosed curly brackets. Expected the character '}' but got '%s'"),
	UNCLOSED_EXPRESSION_PARENTHESES("Unclosed parentheses. Expected the character ')' but got '%s'"),
	UNCLOSED_STATEMENT_PARENTHESES("Unclosed parentheses. Expected the character ')' but got '%s'"),
	UNCLOSED_CALL_PARENTHESES("Unclosed call parentheses. Expected the character ')' but got '%s'"),
	UNCLOSED_CAST_PARENTHESES("Unclosed cast parentheses. Expected the character ')' but got '%s'"),
	UNCLOSED_VARIABLE_DECLARATION("Unclosed variable declaration. Did you forget a semicolon here?"),
	
	
	FLOATING_TYPES_NOT_IMPLEMENTED("Float data types are not implemented yet"),
	
	EXPRESSION_NESTED_TOO_DEEP("Expression was nested too deep.");
	;
	
	public final String message;
	private CompilerError(String value) {
		this.message = value;
	}
	
	public String getMessage() {
		return message;
	}
}
