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
		IF_ELSE,
		LABEL,
		GOTO,
		RETURN,
		CONTINUE,
		BREAK,
		
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
		NONE
	}

	
	// Type of statement
	Type getType();
	
	// Get all elements inside this statement
	List<Statement> getStatements();
	
	// Get the line index of the statement
	int getLineIndex();
	
	// Get the colum index of the statement
	int getColumnIndex();
}
