package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.context.LangReader;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.scope.ProgramScope;
import me.hardcoded.compiler.parser.type.Operator;
import me.hardcoded.compiler.parser.type.OperatorType;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExprParser {
	public static class Group {
		public Operator operator;
		public Token token;
		
		public Group(Operator operator, Token token) {
			this.token = token;
			this.operator = operator;
		}
	}
	
	private final ProgramScope context;
	private final LangReader reader;
	
	public ExprParser(ProgramScope context, LangReader reader) {
		this.context = context;
		this.reader = reader;
	}
	
	// Read until no more input is valid
	/*
	public Expr parse(ProgramScope context, LangReader reader, boolean allowComma) {
		int startIndex = reader.readerIndex();
		
		boolean lastToken = false;
		List<Group> groups = new ArrayList<>();
		while (reader.type() != Token.Type.SEMICOLON) {
			String tokens = reader.peakString(startIndex - reader.readerIndex(), reader.readerIndex() - startIndex + 1);
			System.out.println("-".repeat(50));
			System.out.printf("[%s]\n", tokens);
			
			boolean prevToken = lastToken;
			lastToken = false;
			
			switch (reader.type()) {
				case INT -> {
					NumExpr expr = new NumExpr(reader.syntaxPosition(), Integer.parseInt(reader.value()));
					reader.advance();
				}
				case IDENTIFIER -> {
					Reference reference = context.getLocalScope().getVariable(reader.value());
					NameExpr expr = new NameExpr(reader.syntaxPosition(), reference);
					reader.advance();
					
					// If this is followed by an `L_PAREN` it's a call
					// If this is followed by an `L_SQUARE` it's an array lookup
				}
				case L_PAREN -> {
					lastToken = true;
					tokenStack.add(Operator.L_PAREN);
					reader.advance();
				}
				case R_PAREN -> {
					for (int i = tokenStack.size() - 1; i >= 0; i--) {
						Operator operator = tokenStack.get(i);
						tokenStack.remove(i);
						
						if (operator == Operator.L_PAREN) {
							break;
						} else {
							output.add(operator);
						}
					}
					reader.advance();
				}
				default -> {
					lastToken = true;
					
					Operator operator;
					if (prevToken) {
						operator = Operator.unary(reader.token());
					} else {
						operator = Operator.infix(reader.token());
					}
					
					if (operator == null) {
						throw createParseException("Unknown operator '%s'", reader.value());
					}
					
					tokenStack.add(operator);
					reader.advance();
				}
			}
			
			int i =32;
			System.out.printf("output: %s\n", output);
			System.out.printf("stack : %s\n", tokenStack);
		}
		
		for (int i = tokenStack.size() - 1; i >= 0; i--) {
			Operator operator = tokenStack.get(i);
			tokenStack.remove(i);
			output.add(operator);
		}
		
		{
			int index = reader.readerIndex();
			String tokens = reader.peakString(startIndex - index, index - startIndex + 1);
			System.out.println("-".repeat(50));
			System.out.printf("[%s]\n", tokens);
			System.out.printf("output: %s\n", output);
			System.out.printf("stack : %s\n", tokenStack);
			System.out.println();
		}
		
		// - (32) + 16
		// 32 <un_minus> 16 <plus>
		
		if (output.isEmpty()) {
			throw createParseException("Invalid expression");
		}
		
		return new NoneExpr(reader.syntaxPosition());
	}
	*/
	
	public Expr parse(boolean allowComma) {
		return parse(allowComma, Operator.MAX_PRECEDENCE);
	}
	
	public Expr parse(boolean allowComma, int precedence) {
		if (precedence == 0) {
			switch (reader.type()) {
				case INT -> {
					NumExpr expr = new NumExpr(reader.syntaxPosition(), Integer.parseInt(reader.value()));
					reader.advance();
					return expr;
				}
//				case IDENTIFIER -> {}
				case L_PAREN -> {
					reader.advance();
					Expr expr = parse(false, Operator.MAX_PRECEDENCE);
					tryMatchOrError(Token.Type.R_PAREN);
					reader.advance();
					return expr;
				}
				default -> {
					throw createParseException("Unknown operator '%s'", reader.value());
				}
			}
		}
		
		List<Operator> operators = Operator.getPrecedence(precedence);
		
		Expr left = null;
		for (Operator operator : operators) {
			if (operator.getOperatorType() != OperatorType.Prefix
				|| operator.getTokenType() != reader.type()) continue;
			Position start = reader.position();
			reader.advance();
			
			Expr value = parse(false, precedence);
			left = new UnaryExpr(
				ISyntaxPosition.of(start, value.getSyntaxPosition().getEndPosition()),
				operator,
				value
			);
		}
		
		if (left == null) {
			left = parse(false, precedence - 1);
		}
		
		for (Operator operator : operators) {
			if (operator.getOperatorType() != OperatorType.Suffix
				|| operator.getTokenType() != reader.type()) continue;
			reader.advance();
			
			left = new UnaryExpr(
				ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()),
				operator,
				left
			);
		}
		
		boolean shouldContinue;
		do {
			shouldContinue = false;
			
			for (Operator operator : operators) {
				if (operator.getOperatorType() != OperatorType.Binary
					|| operator.getTokenType() != reader.type()) continue;
					
				reader.advance();
				shouldContinue = true;
				
				Expr right = parse(false, precedence - 1);
				left = new BinaryExpr(ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()), operator, left, right);
			}
		} while (shouldContinue);
		
		return left;
	}
	
	private ParseException createParseException(String format, Object... args) {
		return createParseException(reader == null ? null : reader.position(), format, args);
	}
	
	private ParseException createParseException(Position position, String format, Object... args) {
		String msg = String.format(format, args);
		
		if (position == null) {
			return new ParseException("(?) (line: ?, column: ?): %s", msg);
		}
		
		return new ParseException("(%s) (line: %d, column: %d): %s", position.file, position.line + 1, position.column + 1, msg);
	}
	
	private boolean tryMatchOrError(Token.Type type) {
		return tryMatchOrError(type, () -> "Expected %s but got %s".formatted(type, reader.type()));
	}
	
	private boolean tryMatchOrError(Token.Type type, Supplier<String> message) {
		if (reader.type() != type) {
			throw createParseException(reader.lastPositionEnd(), message.get());
		}
		
		return true;
	}
}
