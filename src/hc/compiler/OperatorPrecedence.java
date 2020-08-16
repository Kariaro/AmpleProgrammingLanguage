package hc.compiler;

import hardcoded.lexer.Token;

public class OperatorPrecedence {
	
	@FunctionalInterface
	private interface Op {
		int action(Token token);
	}
	
	// TODO: First recursive
	public class Expression {
		
	}
	
	// public 
	/*
	expr: _exp15

	_exp15: _exp14
		  | _exp15 ',' _exp14
		  
	_exp14: _exp13
		  | _exp13 '=' _exp14
		  | _exp13 '+=' _exp14
		  | _exp13 '-=' _exp14
		  | _exp13 '*=' _exp14
		  | _exp13 '/=' _exp14
		  | _exp13 '%=' _exp14
		  | _exp13 '&=' _exp14
		  | _exp13 '|=' _exp14
		  | _exp13 '^=' _exp14
		  | _exp13 '>>=' _exp14
		  | _exp13 '<<=' _exp14

	_exp13: _exp12 | _exp13 '?' _exp13 ':' _exp12
	_exp12: _exp11 | _exp12 '||' _exp11
	_exp11: _exp10 | _exp11 '&&' _exp10
	_exp10: _exp9 | _exp10 '|' _exp9
	_exp9: _exp8 | _exp9 '^' _exp8
	_exp8: _exp7 | _exp8 '&' _exp7
	_exp7: _exp6 | _exp7 '==' _exp6 | _exp7 '!=' _exp6
	_exp6: _exp5 | _exp6 '<' _exp5 | _exp6 '<=' _exp5 | _exp6 '>' _exp5 | _exp6 '>=' _exp5
	_exp5: _exp4 | _exp5 '>>' _exp4 | _exp5 '<<' _exp4
	_exp4: _exp3 | _exp4 '+' _exp3 | _exp4 '-' _exp3
	_exp3: _exp2 | _exp3 '*' _exp2 | _exp3 '/' _exp2 | _exp3 '%' _exp2
	_exp2: _exp1
		| '~' _exp1
		| '&' _exp1
		| '*' _exp1
		| '!' _exp1
		| '-' _exp1
		| '?' _exp1
		| '++' _exp1
		| '--' _exp1
		| _exp1 '++'
		| _exp1 '--'
		| '(' type ')' _exp2
	_exp1: _exp0
		| _exp1 '[' expr ']'
	    | _exp1 '(' expr [',' expr] ')'
	_exp0: INTEGERLITERAL
		| STRINGLITERAL
		| IDENTIFIER
		| '(' expr ')'
	*/
}
