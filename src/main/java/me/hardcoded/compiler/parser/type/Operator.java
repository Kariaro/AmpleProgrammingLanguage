package me.hardcoded.compiler.parser.type;

import me.hardcoded.lexer.Token;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum Operator {
	C_OR       ("||", 12, Token.Type.COR,        OperatorType.Binary, Associativity.Left),
	C_AND      ("&&", 11, Token.Type.CAND,       OperatorType.Binary, Associativity.Left),
	OR         ("|",  10, Token.Type.OR,         OperatorType.Binary, Associativity.Left),
	// XOR
	AND        ("&",   8, Token.Type.AND,        OperatorType.Binary, Associativity.Left),
	EQUAL      ("==",  7, Token.Type.EQUALS,     OperatorType.Binary, Associativity.Left),
	NOT_EQUAL  ("!=",  7, Token.Type.NOT_EQUALS, OperatorType.Binary, Associativity.Left),
	LESS_THAN  ("<",   6, Token.Type.LESS_THAN,  OperatorType.Binary, Associativity.Left),
	LESS_EQUAL ("<=",  6, Token.Type.LESS_EQUAL, OperatorType.Binary, Associativity.Left),
	MORE_THAN  (">",   6, Token.Type.MORE_THAN,  OperatorType.Binary, Associativity.Left),
	MORE_EQUAL (">=",  6, Token.Type.MORE_EQUAL, OperatorType.Binary, Associativity.Left),
	// SHIFT LR
	PLUS       ("+",   4, Token.Type.PLUS,       OperatorType.Binary, Associativity.Left),
	MINUS      ("-",   4, Token.Type.MINUS,      OperatorType.Binary, Associativity.Left),
	MULTIPLY   ("*",   3, Token.Type.MUL,        OperatorType.Binary, Associativity.Left),
	DIVIDE     ("/",   3, Token.Type.DIV,        OperatorType.Binary, Associativity.Left),
	// MODULO
	NEGATIVE   ("-",   2, Token.Type.MINUS,      OperatorType.Prefix, Associativity.Right),
	NOT        ("!",   2, Token.Type.NOT,        OperatorType.Suffix, Associativity.Left),
	;
	
	public static final int MAX_PRECEDENCE = Arrays.stream(values()).mapToInt(Operator::getPrecedence).max().orElse(0);
	public static final Map<Integer, List<Operator>> OPERATORS = IntStream.rangeClosed(0, MAX_PRECEDENCE)
		.boxed().collect(Collectors.toMap(i -> i, Operator::getPrecedence0));
	
	private final String name;
	private final int precedence;
	private final Token.Type tokenType;
	private final OperatorType operatorType;
	private final Associativity associativity;
	
	Operator(String name) {
		this(name, 0, Token.Type.WHITESPACE, OperatorType.None, Associativity.None);
	}
	
	Operator(String name, int precedence, Token.Type tokenType, OperatorType operatorType, Associativity associativity) {
		this.name = name;
		this.precedence = precedence;
		this.tokenType = tokenType;
		this.operatorType = operatorType;
		this.associativity = associativity;
	}
	
	public Token.Type getTokenType() {
		return tokenType;
	}
	
	public Associativity getAssociativity() {
		return associativity;
	}
	
	public OperatorType getOperatorType() {
		return operatorType;
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
	
	private static List<Operator> getPrecedence0(int precedence) {
		return Arrays.stream(values()).filter(v -> v.precedence == precedence).toList();
	}
	
	public static List<Operator> getPrecedence(int precedence) {
		return OPERATORS.getOrDefault(precedence, List.of());
	}
	
	/*
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
			case LESS_EQUAL -> Operator.LESS_EQUAL;
			case MORE_THAN -> Operator.MORE_THAN;
			case MORE_EQUAL -> Operator.MORE_EQUAL;
			case NOT -> Operator.NOT;
			default -> null;
		};
	}
	*/
}
