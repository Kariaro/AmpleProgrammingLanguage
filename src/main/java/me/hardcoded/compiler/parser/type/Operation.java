package me.hardcoded.compiler.parser.type;

import me.hardcoded.compiler.parser.ParseException;
import me.hardcoded.lexer.Token;

public enum Operation {
	REFERENCE("&"),
	DEREFERENCE("*"),
	UNARY_PLUS("+"),
	UNARY_MINUS("-"),
	UNARY_NOT("!"),
	MULTIPLY("*"),
	ADD("+"),
	SUBTRACT("-"),
	DIVIDE("/"),
	MODULO("%"),
	AND("&"),
	OR("|"),
	UNARY_NOR("~"),
	XOR("^"),
	MEMBER("."),
	SHIFT_LEFT("<<"),
	SHIFT_RIGHT(">>"),
	CONDITIONAL_AND("&&"),
	CONDITIONAL_OR("||"),
	
	PRE_INCREMENT("++"),
	PRE_DECREMENT("--"),
	POST_INCREMENT("++"),
	POST_DECREMENT("--"),
	
	// Comparison
	EQUALS("=="),
	NOT_EQUALS("!="),
	LESS_THAN("<"),
	LESS_THAN_EQUALS("<="),
	MORE_THAN(">"),
	MORE_THAN_EQUALS(">="),
	
	// Memory
	ASSIGN("="),
	ARRAY("[]"),
	;
	
	private final String name;
	private Operation(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static Operation unaryPrefix(Token.Type token) {
		return switch (token) {
			case PLUS -> UNARY_PLUS;
			case MINUS -> UNARY_MINUS;
			case NOT -> UNARY_NOT;
			case NOR -> UNARY_NOR;
			case AND -> REFERENCE;
			case MUL -> DEREFERENCE;
			case INCREMENT -> PRE_INCREMENT;
			case DECREMENT -> PRE_DECREMENT;
			default -> throw new ParseException("Invalid unary prefix token '%s'", token);
		};
	}
	
	public static Operation unarySuffix(Token.Type token) {
		return switch (token) {
			case INCREMENT -> POST_INCREMENT;
			case DECREMENT -> POST_DECREMENT;
			default -> throw new ParseException("Invalid unary suffix token '%s'", token);
		};
	}
	
	public static Operation conditional(Token.Type token) {
		return switch (token) {
			case CAND -> CONDITIONAL_AND;
			case COR -> CONDITIONAL_OR;
			default -> throw new ParseException("Invalid conditional token '%s'", token);
		};
	}
	
	public static Operation shift(Token.Type token) {
		return switch (token) {
			case SHIFT_LEFT -> SHIFT_LEFT;
			case SHIFT_RIGHT -> SHIFT_RIGHT;
			default -> throw new ParseException("Invalid shift token '%s'", token);
		};
	}
	
	public static Operation arithmetic(Token.Type token) {
		return switch (token) {
			case PLUS -> ADD;
			case MINUS -> SUBTRACT;
			default -> throw new ParseException("Invalid arithmetic token '%s'", token);
		};
	}
	
	public static Operation factor(Token.Type token) {
		return switch (token) {
			case MUL -> MULTIPLY;
			case DIV -> DIVIDE;
			case MOD -> MODULO;
			default -> throw new ParseException("Invalid factor token '%s'", token);
		};
	}
	
	public static Operation comparison(Token.Type token) {
		return switch (token) {
			case EQUALS -> EQUALS;
			case NOT_EQUALS -> NOT_EQUALS;
			case LESS_THAN -> LESS_THAN;
			case LESS_THAN_EQUAL -> LESS_THAN_EQUALS;
			case MORE_THAN -> MORE_THAN;
			case MORE_THAN_EQUAL -> MORE_THAN_EQUALS;
			default -> throw new ParseException("Invalid comparison token '%s'", token);
		};
	}
}
