package me.hardcoded.compiler.parser.type;

import me.hardcoded.lexer.Token;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum Operation {
	// @formatter:off
	ASSIGN      ("=",  13, Token.Type.ASSIGN,      OperationType.Binary, Associativity.Right),
	C_OR        ("||", 12, Token.Type.COR,         OperationType.Binary, Associativity.Right),
	C_AND       ("&&", 11, Token.Type.CAND,        OperationType.Binary, Associativity.Right),
	OR          ("|",  10, Token.Type.OR,          OperationType.Binary, Associativity.Left),
	XOR         ("^",   9, Token.Type.XOR,         OperationType.Binary, Associativity.Left),
	AND         ("&",   8, Token.Type.AND,         OperationType.Binary, Associativity.Left),
	EQUAL       ("==",  7, Token.Type.EQUALS,      OperationType.Binary, Associativity.Left),
	NOT_EQUAL   ("!=",  7, Token.Type.NOT_EQUALS,  OperationType.Binary, Associativity.Left),
	LESS_THAN   ("<",   6, Token.Type.LESS_THAN,   OperationType.Binary, Associativity.Left),
	LESS_EQUAL  ("<=",  6, Token.Type.LESS_EQUAL,  OperationType.Binary, Associativity.Left),
	MORE_THAN   (">",   6, Token.Type.MORE_THAN,   OperationType.Binary, Associativity.Left),
	MORE_EQUAL  (">=",  6, Token.Type.MORE_EQUAL,  OperationType.Binary, Associativity.Left),
	SHIFT_LEFT  (">>",  5, Token.Type.SHIFT_LEFT,  OperationType.Binary, Associativity.Left),
	SHIFT_RIGHT ("<<",  5, Token.Type.SHIFT_RIGHT, OperationType.Binary, Associativity.Left),
	PLUS        ("+",   4, Token.Type.PLUS,        OperationType.Binary, Associativity.Left),
	MINUS       ("-",   4, Token.Type.MINUS,       OperationType.Binary, Associativity.Left),
	MULTIPLY    ("*",   3, Token.Type.MUL,         OperationType.Binary, Associativity.Left),
	DIVIDE      ("/",   3, Token.Type.DIV,         OperationType.Binary, Associativity.Left),
	MODULO      ("%",   3, Token.Type.MOD,         OperationType.Binary, Associativity.Left),
	NEGATIVE    ("-",   2, Token.Type.MINUS,       OperationType.Unary, Associativity.Right),
	NOT         ("!",   2, Token.Type.NOT,         OperationType.Unary, Associativity.Left),
	
	// SPECIAL
	ARRAY       ("[]",  5, Token.Type.L_SQUARE,    OperationType.SpecialBinary, Associativity.Left),
	// @formatter:on
	;
	
	public static final Operation[] VALUES = values();
	public static final int MAX_PRECEDENCE = Arrays.stream(VALUES).mapToInt(Operation::getPrecedence).max().orElse(0);
	private static final Map<Integer, List<Operation>> OPERANDS = IntStream.rangeClosed(0, MAX_PRECEDENCE)
		.boxed().collect(Collectors.toMap(i -> i, i -> Arrays.stream(VALUES).filter(v -> v.precedence == i).toList()));
	
	private final String name;
	private final int precedence;
	private final Token.Type tokenType;
	private final OperationType operationType;
	private final Associativity associativity;
	
	Operation(String name, int precedence, Token.Type tokenType, OperationType operationType, Associativity associativity) {
		this.name = name;
		this.precedence = precedence;
		this.tokenType = tokenType;
		this.operationType = operationType;
		this.associativity = associativity;
	}
	
	public Token.Type getTokenType() {
		return tokenType;
	}
	
	public Associativity getAssociativity() {
		return associativity;
	}
	
	public OperationType getOperationType() {
		return operationType;
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
	
	public static List<Operation> getPrecedence(int precedence) {
		return OPERANDS.getOrDefault(precedence, List.of());
	}
}
