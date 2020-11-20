package hardcoded.compiler.errors;

public enum CompilerError {
	NONE("Missing error message!"),
	MESSAGE("%s"),
	INTERNAL_ERROR("Internal error: %s"),
	
	
	INVALID_IMPORT_EXPECTED_STRING("Invalid import syntax. Expected a string but got '%s'"),
	INVALID_IMPORT_EXPECTED_SEMICOLON("Invalid import syntax. Expected a semicolon but got '%s'"),
	
	INVALID_SET_PROCESSOR_NAME("Invalid set processor. The name '%s' is not valid"),
	INVALID_SET_PROCESSOR_EXPECTED_SEMICOLON("Invalid @set processor. Expected a semicolon ';' but got '%s'"),
	
	INVALID_UNSET_PROCESSOR_EXPECTED_SEMICOLON("Invalid @unset processor. Expected a semicolon ';' but got '%s'"),
	INVALID_UNSET_PROCESSOR_NO_PRIMITIVES("Invalid @unset processor. You cannot unset the primitive type '%s'"),
	
	INVALID_TYPE_PROCESSOR_NAME("Invalid @type processor. The value '%s' is not a valid type name"),
	INVALID_TYPE_PROCESSOR_REDECLARATION("Invalid @type processor. The type '%s' has already been declared"),
	INVALID_TYPE_PROCESSOR_EXPECTED_SEMICOLON("Invalid @type processor. Expected a semicolon but got '%s'"),
	
	INVALID_MODIFICATION("The value you are trying to modify is not modifiable"),
	INVALID_CHAR_LITERAL_SIZE("Invalid character constant"),
	INVALID_CAST_TYPE("Invalid cast type. You cannot cast into a '%s'"),
	INVALID_TYPE("Invalid type '%s'"),
	INVALID_XXX_EXPECTED_OPEN_PARENTHESIS("Invalid %s. Expected open parenthesis '(' but got '%s'"),
	INVALID_XXX_EXPECTED_SEMICOLON("Invalid %s. Expected a semicolon ';' but got '%s'"),
	
	INVALID_CONTINUE_STATEMENT_EXPECTED_SEMICOLON("Invalid continue statement. Expected a semicolon ';' but got '%s'"),
	INVALID_RETURN_STATEMENT_EXPECTED_SEMICOLON("Invalid return statement. Expected a semicolon ';' but got '%s'"),
	INVALID_BREAK_STATEMENT_EXPECTED_SEMICOLON("Invalid break statement. Expected a semicolon ';' but got '%s'"),
	
	INVALID_GOTO_STATEMENT_EXPECTED_SEMICOLON("Invalid goto statement. Expected a semicolon ';' but got '%s'"),
	INVALID_GOTO_LABEL_NAME("Invalid goto label name '%s'"),
	INVALID_GOTO_LABEL_NOT_FOUND("Invalid goto statement. The label '%s' was not found"),
	
	INVALID_LABEL_NAME("Invalid label name '%s'"),
	INVALID_LABEL_REDECLARATION("Invalid label redeclaration. A label '%s' already exists"),
	
	INVALID_WHILE_STATEMENT_EXPECTED_OPEN_PARENTHESIS("Invalid while statement. Expected open parenthesis '(' but got '%s'"),
	INVALID_WHILE_STATEMENT_UNCLOSED_PARENTHESES("Invalid while statement. Unclosed parentheses"),
	INVALID_FOR_STATEMENT_EXPECTED_OPEN_PARENTHESIS("Invalid for statement. Expected open parenthesis '(' but got '%s'"),
	INVALID_IF_STATEMENT_EXPECTED_OPEN_PARENTHESIS("Invalid if statement. Expected open parenthesis '(' but got '%s'"),
	
	
	INVALID_VARIABLE_NAME("Invalid variable name. The name '%s' is not a valid variable name"),
	INVALID_VARIABLE_TYPE("Invalid variable type. The type '%s' is not a valid variable type"),
	INVALID_VARIABLE_ASSIGNMENT("Invalid variable definition. %s"),
	
	INVALID_UNARY_EXPRESSION_OPERATION("Invalid use of a unary expression. The value you are trying to modify is not modifiable"),
	INVALID_DECPTR_EXPRESSION_OPERATION("Invalid decptr expression. You cannot dereference a non pointer value"),
	INVALID_DEREFERENCE_EXPRESSION("Invalid expression. Cannot dereference a non pointer value"),
	INVALID_EXPRESSION("Invalid expression"),
	INVALID_EXPRESSION_MESSAGE("Invalid expression %s"),
	INVALID_EXPR_STATEMENT_EXPECTED_SEMICOLON("Invalid expr statement. Expected a semicolon ';' but got '%s'"),
	
	INVALID_FUNCTION_TYPE("Invalid function type '%s'"),
	INVALID_FUNCTION_DECLARATION_EXPECTED_XXX("Invalid function declaration. '%s'"),
	INVALID_FUNCTION_CALL_PARAMETER("Invalid function call parameter"),
	INVALID_FUNCTION_CALL_EXPRESSION("Invalid function call expression '%s'"),
	INVALID_FUNCTION_NAME("Invalid function name. The name '%s' is not a valid function name"),
	INVALID_FUNCTION_PARAMETER_NAME("Invalid function parameter name '%s'"),
	INVALID_FUNCTION_DECLARATION_EXPECTED_OPEN_PARENTHESIS("Invalid function declaration. Expected open parenthesis '(' but got '%s'"),
	INVALID_FUNCTION_DECLARATION_EXPECTED_CLOSING_PARENTHESIS("Invalid function declaration. Expected closing parenthesis ')' but got '%s'"),
	INVALID_FUNCTION_DECLARATION_EXPECTED_A_FUNCTION_BODY("Invalid function declaration. Expected a function body"),
	INVALID_FUNCTION_DECLARATION_EXPECTED_OPEN_CURLYBRACKET("Invalid function declaration. Expected open bracket '{'"),
	INVALID_FUNCTION_DECLARATION_WRONG_RETURN_TYPE("Invalid function declaration. The return type of the defined function is of the wrong type. Expected '%s'"),
	INVALID_FUNCTION_DECLARATION_WRONG_MODIFIERS("Invalid function declaration. Modifiers are different '%s', expected '%s'"),
	INVALID_FUNCTION_REDECLARATION("Invalid function redeclaration. A function named '%s' already exists"),
	
	REDECLARATION_OF_FUNCTION_PARAMETER("Redeclaration of function parameter '%s'"),
	MISSING_FUNCTION_PARAMETER_SEPARATOR("Invalid function parameter separator. Expected a comma ',' but got '%s'"),
	TOO_MANY_FUNCTION_CALL_ARGUMENTS("Too many arguments calling function '%s' expected %s"),
	NOT_ENOUGH_FUNCTION_CALL_ARGUMENTS("Not enough arguments to call function '%s' expected %s but got %d"),
	
	INVALID_TERNARY_OPERATOR_MISSING_COLON("Invalid ternary operation a ? b : c. Did you forget a colon here? '%s'"),
	INVALID_VARIABLE_DECLARATION_MISSING_COLON_OR_SEMICOLON("Invalid variable definition. Expected a comma or semicolon but got '%s'"),
	
	INVALID_ARRAY_VARIABLE_DECLARATION_EXPECTED_INTEGER("Invalid array variable definition. Expected an integer expression but got '%s'"),
	INVALID_ARRAY_NOT_A_POINTER("Invalid array access. The value you are trying to read from is not a pointer"),
	
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
	
	EXPRESSION_NESTED_TOO_DEEP("Expression was nested too deep"),
	;
	
	public final String message;
	private CompilerError(String value) {
		this.message = value;
	}
	
	public String getMessage() {
		return message;
	}
}
