package com.hardcoded.compiler.api;

import java.util.List;

/**
 * API access
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public interface Statement {
	public enum Type {
		// Code statements
		WHILE,
		FOR,
		DO_WHILE,
		IF,
		LABEL,
		GOTO,
		RETURN,
		CONTINUE,
		BREAK,
		
		SWITCH,
		CASE,
		
		// Expression
		EXPR,
		
		// LAMBDA,
		
		// Top level statements
		CLASS,
		DEFINE,
		FUNCTION,
		IMPORT,
		
		// Root statement
		PROGRAM,
		
		// None statement. Does nothing
		NONE,
		SCOPE,
		
		// Custom statements
		KEYWORD,
	}

	
	// Type of statement
	Type getType();
	
	// Get all elements inside this statement
	List<Statement> getStatements();
	
	/**
	 * Returns the start offset of this statement.
	 * @return the start offset of this statement
	 */
	int getStartOffset();
	
	/**
	 * Returns the end offset of this statement.
	 * @return the end offset of this statement
	 */
	int getEndOffset();
}
