package hc.parser;

public enum SyntaxType {
	// The top of the tree
	ROOT,
	
	// For build commands
	IMPORT,
	DEFINE,
	
	// Method Root
	METHOD,
	ARGUMENTS,
	
	// Container
	BODY,
	
	// Primitive
	PRIMITIVE,
	TYPE,
	POINTER,
	
	// Struct Root
	STRUCT,
	FIELD,
	
	// Class Root
	CLASS,
	ENUM,
	
	// Names
	IDENTIFIER,
	
	MODIFIERS,
	KEYWORD,
	
	ASSIGN,  // a = b
	COMPARE, // a <> b
	EXPR,    // a * b + c
	
	VARIABLE,
	
	STRINGLITERAL,
	INTEGERLITERAL,
	LITERAL,
}
