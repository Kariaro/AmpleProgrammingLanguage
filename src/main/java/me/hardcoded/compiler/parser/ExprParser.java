package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.context.LangReader;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.scope.ProgramScope;
import me.hardcoded.compiler.parser.type.Associativity;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.OperationType;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExprParser {
	private final ProgramScope context;
	private final LangReader reader;
	
	public ExprParser(ProgramScope context, LangReader reader) {
		this.context = context;
		this.reader = reader;
	}
	
	public Expr parse(boolean allowComma) {
		return parse(allowComma, Operation.MAX_PRECEDENCE);
	}
	
	// At some point in the future. Unroll this and make it not recursive
	public Expr parse(boolean allowComma, int precedence) {
		if (precedence == 0) {
			return atomExpression();
		}
		
		List<Operation> operators = Operation.getPrecedence(precedence);
		
		// Prefix
		Expr left = null;
		for (Operation operation : operators) {
			if (operation.getOperationType() != OperationType.Unary
				|| operation.getAssociativity() != Associativity.Right
				|| operation.getTokenType() != reader.type()) continue;
			Position start = reader.position();
			reader.advance();
			
			Expr value = parse(false, precedence);
			left = new UnaryExpr(
				ISyntaxPosition.of(start, value.getSyntaxPosition().getEndPosition()),
				operation,
				value
			);
		}
		
		if (left == null) {
			left = parse(false, precedence - 1);
		}
		
		// Suffix
		boolean shouldContinue;
		do {
			shouldContinue = false;
			for (Operation operation : operators) {
				if (operation.getOperationType() != OperationType.Unary
					|| operation.getAssociativity() != Associativity.Left
					|| operation.getTokenType() != reader.type()) continue;
				reader.advance();
				shouldContinue = true;
				
				left = new UnaryExpr(
					ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()),
					operation,
					left
				);
			}
		} while (shouldContinue);
		
		// Binary
		do {
			shouldContinue = false;
			
			for (Operation operation : operators) {
				if (operation.getOperationType() != OperationType.Binary
					|| operation.getTokenType() != reader.type()) continue;
					
				reader.advance();
				shouldContinue = true;
				
				Expr right;
				if (operation.getAssociativity() == Associativity.Right) {
					right = parse(false, precedence);
				} else {
					right = parse(false, precedence - 1);
				}
				
				left = new BinaryExpr(ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()), operation, left, right);
			}
		} while (shouldContinue);
		
		return left;
	}
	
	// Atom expression
	//
	// This is the lowest part of an expression.
	// The leaf branches of the expression tree.
	private Expr atomExpression() {
		switch (reader.type()) {
			case INT -> {
				NumExpr expr = new NumExpr(reader.syntaxPosition(), Integer.parseInt(reader.value()));
				reader.advance();
				return expr;
			}
			case IDENTIFIER -> {
				if (reader.peak(1).type == Token.Type.L_PAREN) {
					return callExpression();
				}
				
				Reference reference = context.getLocalScope().getVariable(reader.value());
				if (reference == null) {
					throw createParseException("Could not find the variable '%s'", reader.value());
				}
				
				NameExpr expr = new NameExpr(reader.syntaxPosition(), reference);
				reader.advance();
				return expr;
			}
			case L_PAREN -> {
				reader.advance();
				Expr expr = parse(false, Operation.MAX_PRECEDENCE);
				tryMatchOrError(Token.Type.R_PAREN);
				reader.advance();
				return expr;
			}
			default -> {
				throw createParseException("Unknown atom expression '%s'", reader.value());
			}
		}
	}
	
	private Expr callExpression() {
		Position startPos = reader.position();
		
		String name = reader.value();
		reader.advance();
		
		tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		List<Expr> parameters = new ArrayList<>();
		while (reader.type() != Token.Type.R_PAREN) {
			parameters.add(parse(false));
			
			if (reader.type() == Token.Type.COMMA) {
				reader.advance();
				if (reader.type() == Token.Type.R_PAREN) {
					throw createParseException("Invalid comma before ')'");
				}
			} else {
				break;
			}
		}
		
		// TODO: Get function with the specified parameters
		//       and the specified types.
		Reference reference = context.getFunctionScope().getFunction(name);
		
		tryMatchOrError(Token.Type.R_PAREN);
		reader.advance();
		
		return new CallExpr(ISyntaxPosition.of(startPos, reader.lastPositionEnd()), reference, parameters);
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
