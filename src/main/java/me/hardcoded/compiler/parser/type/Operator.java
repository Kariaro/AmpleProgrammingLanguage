package me.hardcoded.compiler.parser.type;

import me.hardcoded.lexer.Token;

public enum Operator {
	PLUS       ("+",  2, 2, Associativity.Left),
	MINUS      ("-",  2, 2, Associativity.Left),
	MULTIPLY   ("*",  2, 3, Associativity.Left),
	DIVIDE     ("/",  2, 3, Associativity.Left),
	AND        ("&",  2, 3, Associativity.Left),
	C_AND      ("&&", 2, 3, Associativity.Left),
	C_OR       ("||", 2, 3, Associativity.Left),
	
	// Unary
	NEGATIVE   ("-",  1, 10, Associativity.Right),
	NOT        ("!",  1, 10, Associativity.Left),
	
	// Comparison
	LESS_THAN  ("<",  2, 4, Associativity.Left),
	LESS_EQUAL ("<=", 2, 4, Associativity.Left),
	MORE_THAN  (">",  2, 4, Associativity.Left),
	MORE_EQUAL (">=", 2, 4, Associativity.Left),
	EQUAL      ("==", 2, 4, Associativity.Left),
	NOT_EQUAL  ("!=", 2, 4, Associativity.Left),
	
	// Empty
	L_PAREN    ("("),
	R_PAREN    (")"),
	;
	
	public static final Operator[] VALUES = values();
	
	private final String name;
	private final int elements;
	private final int precedence;
	private final Associativity associativity;
	
	Operator(String name) {
		this(name, 0, 0, Associativity.None);
	}
	
	Operator(String name, int elements, int precedence, Associativity associativity) {
		this.name = name;
		this.elements = elements;
		this.precedence = precedence;
		this.associativity = associativity;
	}
	
	public Associativity getAssociativity() {
		return associativity;
	}
	
	public int getElements() {
		return elements;
	}
	
	public int getPrecedence() {
		return precedence;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "{ value: '" + name + "', id: " + name() + " }";
	}
	
	public static Operator unary(Token token) {
		return switch (token.type) {
			case MINUS -> Operator.NEGATIVE;
			case NOT -> Operator.NOT;
			default -> null;
		};
	}
	
	public static Operator infix(Token token) {
		return switch (token.type) {
			case MINUS -> Operator.MINUS;
			case PLUS -> Operator.PLUS;
			case MUL -> Operator.MULTIPLY;
			case DIV -> Operator.DIVIDE;
			case EQUALS -> Operator.EQUAL;
			case AND -> Operator.AND;
			case CAND -> Operator.C_AND;
			case COR -> Operator.C_OR;
			case NOT_EQUALS -> Operator.NOT_EQUAL;
			case LESS_THAN -> Operator.LESS_THAN;
			case LESS_THAN_EQUAL -> Operator.LESS_EQUAL;
			case MORE_THAN -> Operator.MORE_THAN;
			case MORE_THAN_EQUAL -> Operator.MORE_EQUAL;
			default -> null;
		};
	}
}
