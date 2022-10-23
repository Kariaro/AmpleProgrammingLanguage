package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.context.LangReader;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.scope.ProgramScope;
import me.hardcoded.compiler.parser.type.*;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ExprParser {
	private static final Logger LOGGER = LogManager.getLogger(ExprParser.class);
	private final AmpleParser parser;
	private final ProgramScope context;
	private final LangReader reader;
	
	public ExprParser(AmpleParser parser, ProgramScope context, LangReader reader) {
		this.parser = parser;
		this.context = context;
		this.reader = reader;
	}
	
	public Expr parse() throws ParseException {
		return parse(Operation.MAX_PRECEDENCE);
	}
	
	// At some point in the future. Unroll this and make it not recursive
	public Expr parse(int precedence) throws ParseException {
		if (precedence == 0) {
			return atomExpression();
		}
		
		List<Operation> operators = Operation.getPrecedence(precedence);
		
		// Prefix
		Expr left = null;
		for (Operation operation : operators) {
			if (operation.getOperationType() != OperationType.Unary
				|| operation.getAssociativity() != Associativity.Right
				|| operation.getTokenType() != reader.type())
				continue;
			Position start = reader.position();
			reader.advance();
			
			Expr value = parse(precedence);
			left = new UnaryExpr(
				ISyntaxPos.of(parser.getCurrentFile(), start, value.getSyntaxPosition().getEndPosition()),
				operation,
				value
			);
		}
		
		if (left == null) {
			left = parse(precedence - 1);
		}
		
		// Suffix
		boolean shouldContinue;
		do {
			shouldContinue = false;
			for (Operation operation : operators) {
				if (operation.getOperationType() != OperationType.Unary
					|| operation.getAssociativity() != Associativity.Left
					|| operation.getTokenType() != reader.type())
					continue;
				reader.advance();
				shouldContinue = true;
				
				left = new UnaryExpr(
					ISyntaxPos.of(parser.getCurrentFile(), left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()),
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
					|| operation.getTokenType() != reader.type())
					continue;
				
				reader.advance();
				shouldContinue = true;
				
				Expr right;
				if (operation.getAssociativity() == Associativity.Right) {
					right = parse(precedence);
				} else {
					right = parse(precedence - 1);
				}
				
				left = new BinaryExpr(ISyntaxPos.of(
					parser.getCurrentFile(),
					left.getSyntaxPosition().getStartPosition(),
					right.getSyntaxPosition().getEndPosition()
				), operation, left, right);
			}
		} while (shouldContinue);
		
		do {
			shouldContinue = false;
			
			for (Operation operation : operators) {
				if (operation.getOperationType() != OperationType.SpecialBinary
					|| operation.getTokenType() != reader.type())
					continue;
				
				reader.advance();
				boolean found = true;
				switch (operation) {
					case ARRAY -> {
						Expr right = parse();
						
						parser.tryMatchOrError(Token.Type.R_SQUARE);
						reader.advance();
						
						left = new BinaryExpr(ISyntaxPos.of(
							parser.getCurrentFile(),
							left.getSyntaxPosition().getStartPosition(),
							reader.lastPositionEnd()
						), operation, left, right);
					}
					default -> found = false;
				}
				
				shouldContinue |= found;
			}
		} while (shouldContinue);
		
		return left;
	}
	
	// Atom expression
	//
	// This is the lowest part of an expression.
	// The leaf branches of the expression tree.
	private Expr atomExpression() throws ParseException {
		switch (reader.type()) {
			case STRING -> {
				ISyntaxPos textSyntaxPosition = reader.syntaxPosition();
				String text = reader.value();
				reader.advance();
				
				try {
					text = text.substring(1, text.length() - 1);
					text = StringUtils.unescapeString(text);
					
					return new StrExpr(textSyntaxPosition, text);
				} catch (Exception e) {
					throw parser.createParseException(textSyntaxPosition,
						"Failed to parse string - %s".formatted(e.getMessage())
					);
				}
			}
			case CHARACTER -> {
				ISyntaxPos charSyntaxPosition = reader.syntaxPosition();
				String text = reader.value();
				reader.advance();
				
				try {
					text = text.substring(1, text.length() - 1);
					text = StringUtils.unescapeString(text);
					
					return new NumExpr(charSyntaxPosition, Primitives.U8, text.charAt(0));
				} catch (Exception e) {
					throw parser.createParseException(charSyntaxPosition,
						"Failed to parse character - %s".formatted(e.getMessage())
					);
				}
			}
			case INT -> {
				String text = reader.value();
				
				int value;
				if (text.startsWith("0x")) {
					value = Integer.parseUnsignedInt(text.substring(2), 16);
				} else {
					value = Integer.parseUnsignedInt(text);
				}
				NumExpr expr = new NumExpr(reader.syntaxPosition(), Primitives.I32, value);
				reader.advance();
				return expr;
			}
			case LONG -> {
				String text = reader.value();
				text = text.substring(0, text.length() - 1);
				
				long value;
				if (text.startsWith("0x")) {
					value = Long.parseUnsignedLong(text.substring(2), 16);
				} else {
					value = Long.parseUnsignedLong(text);
				}
				NumExpr expr = new NumExpr(reader.syntaxPosition(), Primitives.I64, value);
				reader.advance();
				return expr;
			}
			case UINT -> {
				String text = reader.value();
				text = text.substring(0, text.length() - 1);
				
				int value;
				if (text.startsWith("0x")) {
					value = Integer.parseUnsignedInt(text.substring(2), 16);
				} else {
					value = Integer.parseUnsignedInt(text);
				}
				NumExpr expr = new NumExpr(reader.syntaxPosition(), Primitives.U32, value);
				reader.advance();
				return expr;
			}
			case ULONG -> {
				String text = reader.value();
				text = text.substring(0, text.length() - 2);
				
				long value;
				if (text.startsWith("0x")) {
					value = Long.parseUnsignedLong(text.substring(2), 16);
				} else {
					value = Long.parseUnsignedLong(text);
				}
				NumExpr expr = new NumExpr(reader.syntaxPosition(), Primitives.U64, value);
				reader.advance();
				return expr;
			}
			case IDENTIFIER -> {
				// Special functions
				if (isSpecialFunction(reader.value())) {
					return specialCallExpression();
				}
				
				Namespace namespace = parser.readNamespace();
				if (reader.peak(1).type == Token.Type.L_PAREN) {
					return callExpression(namespace);
				}
				
				Reference reference = context.getLocalScope().getVariable(namespace, reader.value());
				if (reference == null) {
					reference = context.getLocalScope().importVariable(namespace, reader.value());
					context.setReferencePosition(reference, reader.syntaxPosition());
					// throw parser.createParseException("Could not find the variable '%s'", reader.value());
				}
				
				NameExpr expr = new NameExpr(reader.syntaxPosition(), reference);
				reader.advance();
				return expr;
			}
			case L_PAREN -> {
				reader.advance();
				Expr expr = parse(Operation.MAX_PRECEDENCE);
				parser.tryMatchOrError(Token.Type.R_PAREN);
				reader.advance();
				return expr;
			}
			default -> {
				throw parser.createParseException("Unknown atom expression '%s'", reader.value());
			}
		}
	}
	
	private Expr callExpression(Namespace namespace) throws ParseException {
		Position startPos = reader.position();
		
		ISyntaxPos nameSyntaxPosition = reader.syntaxPosition();
		String name = reader.value();
		reader.advance();
		
		parser.tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		List<Expr> parameters = new ArrayList<>();
		while (reader.type() != Token.Type.R_PAREN) {
			parameters.add(parse());
			
			if (reader.type() == Token.Type.COMMA) {
				reader.advance();
				if (reader.type() == Token.Type.R_PAREN) {
					throw parser.createParseException("Invalid comma before ')'");
				}
			} else {
				break;
			}
		}
		
		List<Reference> simpleParameters = parameters.stream().map(i -> new Reference("", context.getNamespaceScope().getNamespaceRoot(), i.getType(), 0, 0)).toList();
		
		Reference reference = context.getFunctionScope().getFunction(namespace, name, simpleParameters);
		if (reference == null) {
			reference = context.getFunctionScope().importFunction(namespace, name, simpleParameters);
			context.setReferencePosition(reference, nameSyntaxPosition);
		}
		
		parser.tryMatchOrError(Token.Type.R_PAREN);
		reader.advance();
		
		return new CallExpr(ISyntaxPos.of(parser.getCurrentFile(), startPos, reader.lastPositionEnd()), reference, parameters);
	}
	
	private Expr specialCallExpression() throws ParseException {
		Position startPos = reader.position();
		String name = reader.value();
		reader.advance();
		
		switch (name) {
			case "stack_alloc" -> {
				parser.tryMatchOrError(Token.Type.LESS_THAN);
				reader.advance();
				
				ISyntaxPos typeSyntaxPosition = reader.syntaxPosition();
				ValueType type = parser.readType();
				if (type == null) {
					throw parser.createParseException(typeSyntaxPosition, "Unknown type");
				}
				
				parser.tryMatchOrError(Token.Type.COMMA);
				reader.advance();
				
				parser.tryMatchOrError(Token.Type.INT);
				int size = Integer.parseInt(reader.value());
				reader.advance();
				
				parser.tryMatchOrError(Token.Type.MORE_THAN);
				reader.advance();
				
				parser.tryMatchOrError(Token.Type.L_PAREN);
				reader.advance();
				
				Expr expr;
				// Different data types
				switch (reader.type()) {
					case STRING -> {
						// TODO: Catch exceptions
						try {
							String val = reader.value();
							val = val.substring(1, val.length() - 1);
							val = StringUtils.unescapeString(val);
							
							expr = new StrExpr(reader.syntaxPosition(), val);
							reader.advance();
						} catch (Exception e) {
							throw parser.createParseException(reader.syntaxPosition(), "Failed to unescape string");
						}
					}
					default -> {
						expr = new NoneExpr(reader.syntaxPosition());
					}
				}
				
				parser.tryMatchOrError(Token.Type.R_PAREN);
				reader.advance();
				
				return new StackAllocExpr(ISyntaxPos.of(parser.getCurrentFile(), startPos, reader.lastPositionEnd()), type, size, expr);
			}
			case "cast" -> {
				parser.tryMatchOrError(Token.Type.LESS_THAN);
				reader.advance();
				
				ValueType type = parser.readType();
				
				parser.tryMatchOrError(Token.Type.MORE_THAN);
				reader.advance();
				
				parser.tryMatchOrError(Token.Type.L_PAREN);
				reader.advance();
				
				Expr expr = parse();
				
				parser.tryMatchOrError(Token.Type.R_PAREN);
				reader.advance();
				
				return new CastExpr(ISyntaxPos.of(parser.getCurrentFile(), startPos, reader.lastPositionEnd()), type, expr);
			}
		}
		
		return new NoneExpr(reader.syntaxPosition());
	}
	
	private boolean isSpecialFunction(String name) {
		return name.equals("stack_alloc")
			|| name.equals("cast");
	}
}
