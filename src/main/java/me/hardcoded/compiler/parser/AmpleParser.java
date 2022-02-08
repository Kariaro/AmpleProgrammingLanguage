package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.context.LangContext;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.scope.ProgramScope;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.*;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.lexer.LexerTokenizer;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.MutableSyntaxImpl;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class is responsible for creating the abstract syntax tree of the arucas programming language.
 * No optimizations should be applied in this parser and no type checking should be done.
 *
 * This parser will create a {@link LinkableObject} that contains:
 * <ul>
 *   <li>Imports</li>
 *   <li>Unresolved references</li>
 *   <li>Syntax tree</li>
 * </ul>
 *
 * @author HardCoded
 */
public class AmpleParser {
	private CompilerConfiguration config;
	private final ProgramScope currentScope;
	private final List<String> importedFiles;
	
	private File currentFile;
	private LangContext reader;
	
	public AmpleParser() {
		this.currentScope = new ProgramScope();
		this.importedFiles = new ArrayList<>();
	}
	
	public LinkableObject fromFile(File file) throws IOException {
		return fromBytes(file, Files.readAllBytes(file.toPath()));
	}
	
	private LinkableObject fromBytes(File file, byte[] bytes) {
		if (bytes == null) {
			throw createParseException("Tried to parse an array 'null'");
		}
		
		LangContext oldContext = reader;
		File oldFile = currentFile;
		
		// Update the fields inside this class
		currentFile = file;
		reader = LangContext.wrap(LexerTokenizer.parse(file, bytes));
		
		// Parse the current code
		ProgStat program = parse();
		
		List<Reference> missingReferences = new ArrayList<>();
		List<Reference> exportedReferences = new ArrayList<>();
		for (Reference reference : currentScope.getAllReferences()) {
			// If the reference is not used inside the linkable object we ignore it
			if (reference.getUsages() < 1) {
				continue;
			}
			
			if (reference.isImported()) {
				missingReferences.add(reference);
			}
			
			if (reference.isExported()) {
				exportedReferences.add(reference);
			}
		}
		
		LinkableObject linkableObject = new LinkableObject(file, program, importedFiles, exportedReferences, missingReferences);
		
		reader = oldContext;
		currentFile = oldFile;
		
		return linkableObject;
	}
	
	/**
	 * This is the start of the parsing
	 */
	private ProgStat parse() {
		// This is only called for the root scope of each file
		
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(reader.position(), null);
		ProgStat list = new ProgStat(mutableSyntax);
		
		// Push a new variable block
		currentScope.getFunctionScope().pushBlock();
		currentScope.getLocalScope().pushBlock();
		currentScope.getLabelScope().pushBlock();
		while (reader.remaining() > 0) {
			Stat stat = parseStatement();
			
			if (!stat.isEmpty()) {
				list.addElement(stat);
			}
		}
		mutableSyntax.end = reader.lastPositionEnd();
		
		// Make sure we check lables
		popLablesAndErrorCheck();
		
		currentScope.getLocalScope().popBlock();
		currentScope.getFunctionScope().popBlock();
		
		return list;
	}
	
	/**
	 * Returns a parse statement
	 *
	 * <pre>
	 * ParseStatement ::= LinkStatement
	 *   | NamespaceStatement
	 *   | FunctionStatement
	 *   | Statement
	 * </pre>
	 */
	private Stat parseStatement() {
		if (reader.type() == Token.Type.LINK) {
			Position startPos = reader.position();
			reader.advance();
			
			tryMatchOrError(Token.Type.STRING);
			String importedFile = reader.value();
			
			importedFiles.add(importedFile.substring(1, importedFile.length() - 1));
			reader.advance();
			return new EmptyStat(ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
		}
		
		if (isNamespaceDeclaration()) {
			return namespaceStatement();
		}
		
		if (isFunctionDeclaration()) {
			return functionStatement();
		}
		
		return statement();
	}
	
	/**
	 * Returns a namespace statement
	 */
	private Stat namespaceStatement() {
		Position startPos = reader.position();
		
		boolean isExport = false;
		if (reader.type() == Token.Type.EXPORT) {
			isExport = true;
			reader.advance();
		}
		
		tryMatchOrError(Token.Type.NAMESPACE);
		reader.advance();
		
		tryMatchOrError(Token.Type.IDENTIFIER);
		String name = reader.value();
		
		Reference reference = currentScope.createEmptyReference(name);
		reference.setExported(isExport);
		
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(startPos, null);
		NamespaceStat result = new NamespaceStat(reference, mutableSyntax);
		reader.advance();
		
		tryMatchOrError(Token.Type.LEFT_CURLY_BRACKET);
		reader.advance();
		
		currentScope.getFunctionScope().pushBlock(name);
		
		while (reader.type() != Token.Type.RIGHT_CURLY_BRACKET) {
			Stat stat;
			
			if (isNamespaceDeclaration()) {
				stat = namespaceStatement();
			} else {
				stat = functionStatement();
				
				if (stat instanceof FuncStat funcStat) {
					// Make sure all functions are exported
					funcStat.getReference().setExported(isExport);
				}
			}
			
			result.addElement(stat);
		}
		
		currentScope.getFunctionScope().popBlock();
		
		tryMatchOrError(Token.Type.RIGHT_CURLY_BRACKET);
		reader.advance();
		mutableSyntax.end = reader.lastPositionEnd();
		return result;
	}
	
	/**
	 * Returns a function statement
	 *
	 * <pre>
	 * FunctionStatement ::= ValueType IDENTIFIER '(' FunctionParameters ')' Statements
	 * </pre>
	 */
	private Stat functionStatement() {
		Position startPos = reader.position();
		
		boolean isExport = false;
		if (reader.type() == Token.Type.EXPORT) {
			isExport = true;
			reader.advance();
		}
		
		ValueType returnType = getValueType();
		
		tryMatchOrError(Token.Type.IDENTIFIER);
		String name = reader.value();
		reader.advance();
		
		tryMatchOrError(Token.Type.LEFT_PARENTHESIS);
		reader.advance();
		
		List<Reference> parameters = new ArrayList<>();
		
		currentScope.getLocalScope().pushBlock();
		currentScope.getLabelScope().pushBlock();
		
		while (reader.type() != Token.Type.RIGHT_PARENTHESIS) {
			ValueType paramType = getValueType();
			if (paramType == null) {
				throw createParseException("Invalid parameter type '%s'",  reader.peakString(0, 3));
			}
			
			tryMatchOrError(Token.Type.IDENTIFIER);
			String paramName = reader.value();
			Reference paramReference = currentScope.getLocalScope().addLocalVariable(paramType, paramName);
			
			reader.advance();
			if (paramReference == null) {
				throw createParseException("The parameter '%s' has already been defined", paramName);
			}
			paramReference.setValueType(paramType);
			
			parameters.add(paramReference);
			
			if (reader.type() == Token.Type.COMMA) {
				reader.advance();
				
				if (reader.type() == Token.Type.RIGHT_PARENTHESIS) {
					throw createParseException("Invalid ')'");
				}
			}
		}
		
		// Create the function after the parameters has been parsed
		Reference reference = currentScope.getFunctionScope().addFunction(returnType, name, parameters);
		if (reference == null) {
			throw createParseException("The function '%s' has already been declared", name);
		}
		reference.incUsages();
		reference.setExported(isExport);
		
		tryMatchOrError(Token.Type.RIGHT_PARENTHESIS);
		reader.advance();
		
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(startPos, null);
		FuncStat result = new FuncStat(reference, parameters, mutableSyntax);
		
		Stat body = statements();
		result.complete(body);
		mutableSyntax.end = reader.lastPositionEnd();
		
		currentScope.getLocalScope().popBlock();
		
		// Make sure we check lables
		popLablesAndErrorCheck();
		
		return result;
	}
	
	/**
	 * Returns a group statement
	 *
	 * <pre>
	 * Statements ::= '{' ( '}' | Statement+ '}' )
	 *   | Statement
	 * </pre>
	 */
	private Stat statements() {
		if (reader.type() != Token.Type.LEFT_CURLY_BRACKET) {
			return statement();
		}
		
		Position startPos = reader.position();
		tryMatchOrError(Token.Type.LEFT_CURLY_BRACKET);
		reader.advance();
		
		if (reader.type() == Token.Type.RIGHT_CURLY_BRACKET) {
			reader.advance();
			return new EmptyStat(ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
		}
		
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(startPos, null);
		ScopeStat result = new ScopeStat(mutableSyntax);
		
		currentScope.getLocalScope().pushLocals();
		while (reader.type() != Token.Type.RIGHT_CURLY_BRACKET) {
			Stat stat = statement();
			
			// Do not add empty statements
			if (stat.isEmpty()) {
				continue;
			}
			
			result.addElement(stat);
		}
		currentScope.getLocalScope().popLocals();
		
		reader.advance();
		mutableSyntax.end = reader.lastPositionEnd();
		return result;
	}
	
	/**
	 * Returns a group statements
	 *
	 * <pre>
	 * Statement ::= Statements
	 *   | ';'
	 *   | IfStatement
	 *   | WhileStatement
	 *   | ForStatement
	 *   | LabelStatement
	 *
	 *   | GotoStatement ';'
	 *   | ReturnStatement ';'
	 *   | BreakStatement ';'
	 *   | ContinueStatement ';'
	 *   | Expression ';'
	 * </pre>
	 */
	private Stat statement() {
		if (reader.type() == Token.Type.LEFT_CURLY_BRACKET) {
			return this.statements();
		}
		
		if (reader.type() == Token.Type.SEMICOLON) {
			ISyntaxPosition position = reader.syntaxPosition();
			reader.advance();
			return new EmptyStat(position);
		}
		
		switch (reader.type()) {
			case IF -> {
				return ifStatement();
			}
			case WHILE -> {
				return whileStatement();
			}
			case FOR -> {
				return forStatement();
			}
			case IDENTIFIER -> {
				if (reader.peak(1).type == Token.Type.COLON) {
					Position startPos = reader.position();
					String location = reader.value();
					Reference reference;
					if ((reference = currentScope.getLabelScope().addLabel(location)) == null) {
						throw createParseException("The label '%s' has already been defined", location);
					}
					reader.advance();
					reader.advance();
					return new LabelStat(reference, ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
				}
			}
		}
		
		// If we have a type
		if (isValueTypeAndName()) {
			Stat stat = varStatement();
			
			tryMatchOrError(Token.Type.SEMICOLON);
			reader.advance();
			return stat;
		}
		
		// Must have semicolons
		Stat stat = switch (reader.type()) {
			case RETURN -> {
				Position startPos = reader.position();
				reader.advance();
				
				Expr expr;
				if (reader.type() != Token.Type.SEMICOLON) {
					expr = expression();
				} else {
					expr = new NumExpr(0, Atom.int_8, ISyntaxPosition.of(reader.position(), reader.position()));
				}
				
				yield new ReturnStat(expr, ISyntaxPosition.of(startPos, reader.nextPositionEnd()));
			}
			case BREAK ->  {
				Position startPos = reader.position();
				reader.advance();
				yield new BreakStat(ISyntaxPosition.of(startPos, reader.nextPositionEnd()));
			}
			case CONTINUE ->  {
				Position startPos = reader.position();
				reader.advance();
				yield new ContinueStat(ISyntaxPosition.of(startPos, reader.nextPositionEnd()));
			}
			case GOTO -> {
				Position startPos = reader.position();
				reader.advance();
				
				tryMatchOrError(Token.Type.IDENTIFIER);
				String destination = reader.value();
				reader.advance();
				Reference reference = currentScope.createEmptyReference(destination);
				GotoStat gotoStat = new GotoStat(reference, ISyntaxPosition.of(startPos, reader.nextPositionEnd()));
				currentScope.getLabelScope().addGoto(gotoStat);
				
				yield gotoStat;
			}
			default -> expression();
		};
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		return stat;
	}
	
	/**
	 * Returns a var statement
	 *
	 * <pre>
	 * VarStatement ::= ValueType VarElement ( ',' VarElement )*
	 * VarElement ::= IDENTIFIER [ '=' Expression ]
	 * </pre>
	 */
	private Stat varStatement() {
		Position startPos = reader.position();
		ValueType type = getValueType();
		
		tryMatchOrError(Token.Type.IDENTIFIER);
		String name = reader.value();
		
		Reference reference = currentScope.getLocalScope().addLocalVariable(type, name);
		if (reference == null) {
			throw createParseException("A variable '%s' has already been declared in this scope", name);
		}
		
		reader.advance();
		
		Expr value;
		if (reader.type() == Token.Type.ASSIGN) {
			reader.advance();
			value = expression();
		} else {
			value = new NullExpr(ISyntaxPosition.of(reader.position(), reader.position()));
		}
		
		return new VarStat(reference, value, ISyntaxPosition.of(startPos, reader.nextPositionEnd()));
	}
	
	/**
	 * Returns an if statement
	 *
	 * <pre>
	 * IfStatement ::= 'if' '(' Expression ')' Statements [ 'else' Statements ]
	 * </pre>
	 */
	private Stat ifStatement() {
		Position startPos = reader.position();
		reader.advance();
		
		tryMatchOrError(Token.Type.LEFT_PARENTHESIS);
		reader.advance();
		
		Expr condition = expression();
		
		tryMatchOrError(Token.Type.RIGHT_PARENTHESIS);
		reader.advance();
		
		Position endPos;
		Stat body = statements();
		Stat elseBody;
		
		if (reader.type() == Token.Type.ELSE) {
			elseBody = statements();
			endPos = reader.lastPositionEnd();
		} else {
			endPos = reader.lastPositionEnd();
			elseBody = new EmptyStat(ISyntaxPosition.of(endPos, endPos));
		}
		
		return new IfStat(condition, body, elseBody, ISyntaxPosition.of(startPos, endPos));
	}
	
	/**
	 * Returns a while statement
	 *
	 * <pre>
 	 * WhileStatement ::= 'while' '(' Expression ')' Statements
	 * </pre>
	 */
	private Stat whileStatement() {
		Position startPos = reader.position();
		reader.advance();
		
		tryMatchOrError(Token.Type.LEFT_PARENTHESIS);
		reader.advance();
		
		Expr condition = expression();
		
		tryMatchOrError(Token.Type.RIGHT_PARENTHESIS);
		reader.advance();
		
		Stat body = statements();
		return new WhileStat(condition, body, ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
	}
	
	/**
	 * Returns a for statement
	 *
	 * <pre>
	 * forStatement ::= 'for' '(' Expression? ';' Expression? ';' Expression? ')' Statements
	 * </pre>
	 */
	private Stat forStatement() {
		Position startPos = reader.position();
		reader.advance();
		
		tryMatchOrError(Token.Type.LEFT_PARENTHESIS);
		reader.advance();
		
		Stat start;
		if (reader.type() == Token.Type.SEMICOLON) {
			start = new EmptyStat(ISyntaxPosition.of(reader.position(), reader.position()));
		} else {
			start = varStatement();
		}
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		Expr condition;
		if (reader.type() == Token.Type.SEMICOLON) {
			condition = new NumExpr(1, Atom.int_8, ISyntaxPosition.of(reader.position(), reader.position()));
		} else {
			condition = comparisonExpression();
		}
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		Expr action;
		if (reader.type() == Token.Type.SEMICOLON) {
			action = new NullExpr(ISyntaxPosition.of(reader.position(), reader.position()));
		} else {
			action = comparisonExpression();
		}
		
		tryMatchOrError(Token.Type.RIGHT_PARENTHESIS);
		reader.advance();
		
		Stat body = statements();
		return new ForStat(start, condition, action, body, ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
	}
	
	/**
	 * Returns an expression
	 *
	 * <pre>
	 * Expression ::= CommaExpression
	 *   | SizeExpression
	 * </pre>
	 */
	private Expr expression() {
		Expr left = sizeExpression();
		
		if (reader.type() == Token.Type.COMMA) {
			List<Expr> list = new ArrayList<>();
			list.add(left);
			while (reader.type() == Token.Type.COMMA) {
				reader.advance();
				Expr right = sizeExpression();
				if (right instanceof CommaExpr e) {
					list.addAll(e.getValues());
				} else {
					list.add(right);
				}
			}
			
			return new CommaExpr(list, ISyntaxPosition.of(list.get(0).getSyntaxPosition(), list.get(list.size() - 1).getSyntaxPosition()));
		}
		
		return left;
	}
	
	/**
	 * Returns a size expression
	 *
	 * <pre>
	 * SizeExpression ::= OrExpression [ SizeOp SizeExpresssion ]
	 * SizeOp ::= '&&' | '||'
	 * </pre>
	 */
	private Expr sizeExpression() {
		Expr left = orExpression();
		
		while (true) {
			Token.Type matchType = reader.type();
			if (matchType == Token.Type.CAND || matchType == Token.Type.COR) {
				MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(left.getSyntaxPosition().getStartPosition(), null);
				ConditionalExpr condExpr = new ConditionalExpr(Operation.conditional(matchType), mutableSyntax);
				
				// (a, b, c) && d
				// (a, b, c && d)
				CommaExpr comma;
				if ((comma = getCommaExpr(left)) != null) {
					condExpr.addElement(comma.getLast());
					while (reader.type() == matchType) {
						reader.advance();
						condExpr.addElement(orExpression());
					}
					comma.setLast(condExpr);
				} else {
					condExpr.addElement(left);
					while (reader.type() == matchType) {
						reader.advance();
						condExpr.addElement(orExpression());
					}
					left = condExpr;
				}
				
				mutableSyntax.end = reader.lastPositionEnd();
				// We will try continue matching
				continue;
			}
			
			break;
		}
		
		return left;
	}
	
	/**
	 * Returns an or expression
	 *
	 * <pre>
	 * OrExpression ::= XorExpression ( '|' XorExpression )*
	 * </pre>
	 */
	private Expr orExpression() {
		Expr left = xorExpression();
		
		while (reader.type() == Token.Type.OR) {
			reader.advance();
			Expr right = xorExpression();
			
			CommaExpr comma;
			if ((comma = getCommaExpr(left)) != null) {
				comma.setLast(
					new BinaryExpr(comma.getLast(), right, Operation.OR, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()))
				);
			} else {
				left = new BinaryExpr(left, right, Operation.OR, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()));
			}
		}
		
		return left;
	}
	
	/**
	 * Returns a xor expression
	 *
	 * <pre>
	 * XorExpression ::= AndExpression ( '^' AndExpression )*
	 * </pre>
	 */
	private Expr xorExpression() {
		Expr left = andExpression();
		
		while (reader.type() == Token.Type.XOR) {
			reader.advance();
			Expr right = andExpression();
			
			CommaExpr comma;
			if ((comma = getCommaExpr(left)) != null) {
				comma.setLast(
					new BinaryExpr(comma.getLast(), right, Operation.XOR, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()))
				);
			} else {
				left = new BinaryExpr(left, right, Operation.XOR, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()));
			}
		}
		
		return left;
	}
	
	/**
	 * Returns an and expression
	 *
	 * <pre>
	 * AndExpression ::= ComparisonExpression ( '&' ComparisonExpression )*
	 * </pre>
	 */
	private Expr andExpression() {
		Expr left = comparisonExpression();
		
		while (reader.type() == Token.Type.AND) {
			reader.advance();
			Expr right = comparisonExpression();
			
			CommaExpr comma;
			if ((comma = getCommaExpr(left)) != null) {
				comma.setLast(
					new BinaryExpr(comma.getLast(), right, Operation.AND, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()))
				);
			} else {
				left = new BinaryExpr(left, right, Operation.AND, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()));
			}
		}
		
		return left;
	}
	
	/**
	 * Returns a comparison expression
	 *
	 * <pre>
	 * ComparisonExpression ::= '!' ComparisonExpression
	 *   | ShiftExpression [ ComparisonOp ShiftExpression ]
	 * ComparisonOp ::= '==' | '!=' | '<' | '>' | '<=' | '>='
	 * </pre>
	 */
	private Expr comparisonExpression() {
		if (reader.type() == Token.Type.NOT) {
			Position startPos = reader.position();
			reader.advance();
			Expr expr = comparisonExpression();
			
			CommaExpr comma;
			if ((comma = getCommaExpr(expr)) != null) {
				comma.setLast(
					new UnaryExpr(comma.getLast(), Operation.UNARY_NOT, ISyntaxPosition.of(startPos, expr.getSyntaxPosition().getEndPosition()))
				);
				return expr;
			} else {
				return new UnaryExpr(expr, Operation.UNARY_NOT, ISyntaxPosition.of(startPos, expr.getSyntaxPosition().getEndPosition()));
			}
		}
		
		Expr left = shiftExpression();
		
		Token.Type token = reader.type();
		switch (token) {
			case EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_EQUAL, MORE_THAN, MORE_THAN_EQUAL -> {
				reader.advance();
				Expr expr = shiftExpression();
				
				CommaExpr comma;
				if ((comma = getCommaExpr(left)) != null) {
					comma.setLast(
						new BinaryExpr(comma.getLast(), expr, Operation.comparison(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()))
					);
				} else {
					left = new BinaryExpr(left, expr, Operation.comparison(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()));
				}
			}
		}
		
		return left;
	}
	
	/**
	 * Returns an arithmetic expression
	 *
	 * <pre>
	 * ShiftExpression ::= ArithmeticExpression ( ShiftOp ArithmeticExpression )*
	 * ShiftOp ::= '<<' | '>>'
	 * </pre>
	 */
	private Expr shiftExpression() {
		Expr left = arithmeticExpression();
		
		while (true) {
			Token.Type token = reader.type();
			switch (token) {
				case SHIFT_LEFT, SHIFT_RIGHT -> {
					reader.advance();
					Expr expr = arithmeticExpression();
					
					CommaExpr comma;
					if ((comma = getCommaExpr(left)) != null) {
						comma.setLast(
							new BinaryExpr(comma.getLast(), expr, Operation.shift(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()))
						);
					} else {
						left = new BinaryExpr(left, expr, Operation.shift(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()));
					}
				}
				default -> {
					return left;
				}
			}
		}
	}
	
	/**
	 * Returns an arithmetic expression
	 *
	 * <pre>
	 * ArithmeticExpression ::= TermExpression ( ArithmeticOp TermExpression )*
	 * ArithmeticOp ::= '+' | '-'
	 * </pre>
	 */
	private Expr arithmeticExpression() {
		Expr left = termExpression();
		
		while (true) {
			Token.Type token = reader.type();
			switch (token) {
				case PLUS, MINUS -> {
					reader.advance();
					Expr expr = termExpression();
					
					CommaExpr comma;
					if ((comma = getCommaExpr(left)) != null) {
						comma.setLast(
							new BinaryExpr(comma.getLast(), expr, Operation.arithmetic(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()))
						);
					} else {
						left = new BinaryExpr(left, expr, Operation.arithmetic(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()));
					}
				}
				default -> {
					return left;
				}
			}
		}
	}
	
	/**
	 * Returns a term expression
	 *
	 * <pre>
	 * TermExpression ::= FactorExpression ( TermOp FactorExpression )*
	 * TermOp ::= '*' | '/' | '%'
	 * </pre>
	 */
	private Expr termExpression() {
		Expr left = factorExpression();
		
		while (true) {
			Token.Type token = reader.type();
			switch (token) {
				case MUL, DIV, MOD -> {
					reader.advance();
					Expr expr = factorExpression();
					
					CommaExpr comma;
					if ((comma = getCommaExpr(left)) != null) {
						comma.setLast(
							new BinaryExpr(comma.getLast(), expr, Operation.factor(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()))
						);
					} else {
						left = new BinaryExpr(left, expr, Operation.factor(token), ISyntaxPosition.of(left.getSyntaxPosition(), expr.getSyntaxPosition()));
					}
				}
				default -> {
					return left;
				}
			}
		}
	}
	
	/**
	 * Returns a factor expression
	 *
	 * <pre>
	 * FactorExpression ::= ArrayExpression FactorOps
	 * FactorOps ::= '=' SizeExpression
	 *   | [ FuncParams ]
	 * FuncParams ::= '(' FuncParam ( ',' FuncParam )* ')'
	 * FuncParam ::= SizeExpression
	 * </pre>
	 */
	private Expr factorExpression() {
		Expr left = arrayExpression();
		
		if (reader.type() == Token.Type.ASSIGN) {
			reader.advance();
			Expr right = sizeExpression();
			
			CommaExpr comma;
			if ((comma = getCommaExpr(left)) != null) {
				comma.setLast(
					new BinaryExpr(comma.getLast(), right, Operation.ASSIGN, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()))
				);
				return left;
			} else {
				return new BinaryExpr(left, right, Operation.ASSIGN, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()));
			}
		}
		
		if (reader.type() == Token.Type.LEFT_PARENTHESIS) {
			reader.advance();
			List<Expr> parameters = new ArrayList<>();
			
			while (reader.type() != Token.Type.RIGHT_PARENTHESIS) {
				Expr expr = sizeExpression();
				parameters.add(expr);
				
				if (reader.type() == Token.Type.COMMA) {
					reader.advance();
					
					if (reader.type() == Token.Type.RIGHT_PARENTHESIS) {
						throw createParseException("Invalid ')'");
					}
				}
			}
			
			tryMatchOrError(Token.Type.RIGHT_PARENTHESIS);
			reader.advance();
			
			if (left instanceof NameExpr e) {
				// If this is imported make sure we mark it as a function
				Reference reference = e.getReference();
				if (reference.isImported()) {
					reference.setType(Reference.FUNCTION);
				}
			}
			
			CommaExpr comma;
			if ((comma = getCommaExpr(left)) != null) {
				comma.setLast(
					new CallExpr(comma.getLast(), parameters,  ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()))
				);
			} else {
				left = new CallExpr(left, parameters, ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()));
			}
		}
		
		return left;
	}
	
	/**
	 * Returns a term expression
	 *
	 * <pre>
	 * ArrayExpression ::= ModifierOp MemberExpression
	 *   | MemberExpression ModifierOp
	 *   | MemberExpression ( '[' Expression ']' )*
	 * ModifierOp ::= '++' | '--'
	 * </pre>
	 */
	private Expr arrayExpression() {
		switch (reader.type()) {
			case INCREMENT, DECREMENT -> {
				Position startPos = reader.position();
				Token.Type token = reader.type();
				reader.advance();
				
				Expr expr = memberExpression();
				
				CommaExpr comma;
				if ((comma = getCommaExpr(expr)) != null) {
					comma.setLast(
						new UnaryExpr(comma.getLast(), Operation.unaryPrefix(token), ISyntaxPosition.of(startPos, reader.lastPositionEnd()))
					);
					return expr;
				} else {
					return new UnaryExpr(expr, Operation.unaryPrefix(token), ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
				}
			}
		}
		
		Expr left = memberExpression();
		
		while (true) {
			Token.Type token = reader.type();
			switch (token) {
				case LEFT_SQUARE_BRACKET -> {
					reader.advance();
					Expr right = expression();
					
					tryMatchOrError(Token.Type.RIGHT_SQUARE_BRACKET);
					reader.advance();
					
					CommaExpr comma;
					if ((comma = getCommaExpr(left)) != null) {
						comma.setLast(
							new BinaryExpr(comma.getLast(), right, Operation.ARRAY, ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()))
						);
					} else {
						left = new BinaryExpr(left, right, Operation.ARRAY, ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()));
					}
				}
				case INCREMENT, DECREMENT -> {
					reader.advance();
					
					CommaExpr comma;
					if ((comma = getCommaExpr(left)) != null) {
						comma.setLast(
							new UnaryExpr(comma.getLast(), Operation.unarySuffix(token), ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()))
						);
						return left;
					} else {
						return new UnaryExpr(left, Operation.unarySuffix(token), ISyntaxPosition.of(left.getSyntaxPosition().getStartPosition(), reader.lastPositionEnd()));
					}
				}
				default -> {
					return left;
				}
			}
		}
	}
	
	/**
	 * Returns a member expression
	 *
	 * <pre>
	 * MemberExpression ::= IDENTIFIER ( '.' IDENTIFIER )*
	 *   | AtomExpression
	 * </pre>
	 */
	private Expr memberExpression() {
		if (reader.type() == Token.Type.IDENTIFIER) {
			Expr left = atomExpression();
			
			while (reader.type() == Token.Type.DOT) {
				reader.advance();
				
				tryMatchOrError(Token.Type.IDENTIFIER);
				Expr right = new NameExpr(currentScope.createEmptyReference(reader.value()), reader.syntaxPosition());
				reader.advance();
				
				CommaExpr comma;
				if ((comma = getCommaExpr(left)) != null) {
					comma.setLast(
						new BinaryExpr(comma.getLast(), right, Operation.MEMBER, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()))
					);
				} else {
					left = new BinaryExpr(left, right, Operation.MEMBER, ISyntaxPosition.of(left.getSyntaxPosition(), right.getSyntaxPosition()));
				}
			}
			
			return left;
		}
		
		return unaryExpression();
	}
	
	/**
	 * Returns an unary expression
	 *
	 * <pre>
	 * UnaryExpression ::= UnaryOp UnaryExpression
	 *   | AtomExpression
	 * UnaryOp ::= '&' | '*' | '-' | '+' | '(' ':' ValueType ')'
	 * </pre>
	 */
	private Expr unaryExpression() {
		Token.Type token = reader.type();
		
		switch (token) {
			case AND -> {
				Position startPos = reader.position();
				reader.advance();
				Expr right = unaryExpression();
				
				// In what order we need to compute these:
				//    Local variable
				//    Local labels
				//    Functions
				//    Global variables
				
				NameExpr nameExpr = null;
				CommaExpr comma;
				if ((comma = getCommaExpr(right)) != null) {
					if (comma.getLast() instanceof NameExpr e) {
						nameExpr = e;
					}
				} else if (right instanceof NameExpr e) {
					nameExpr = e;
				}
				
				// Local variables need to be checked first
				if (nameExpr != null) {
					Reference reference = null;
					
					if ((reference = currentScope.getLocalScope().getLocal(nameExpr.geName())) != null) {
						if (nameExpr.getReference() != reference) {
							nameExpr.getReference().decUsages();
							reference.incUsages();
						}
						
						nameExpr.setReference(reference);
					}
					
					if (reference == null) {
						// Add this to a list of unresolved references
						//    Local labels
						//    Functions
						//    Global variablesS
						currentScope.getLabelScope().addMissingLabelReference(nameExpr);
					}
				}
				
				if ((comma = getCommaExpr(right)) != null) {
					comma.setLast(
						new UnaryExpr(comma.getLast(), Operation.REFERENCE, ISyntaxPosition.of(startPos, reader.lastPositionEnd()))
					);
					return right;
				} else {
					return new UnaryExpr(right, Operation.REFERENCE, ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
				}
			}
			case MINUS, PLUS, MUL, NOR -> {
				Position startPos = reader.position();
				reader.advance();
				Expr right = unaryExpression();
				
				CommaExpr comma;
				if ((comma = getCommaExpr(right)) != null) {
					comma.setLast(
						new UnaryExpr(comma.getLast(), Operation.unaryPrefix(token), ISyntaxPosition.of(startPos, reader.lastPositionEnd()))
					);
					return right;
				} else {
					return new UnaryExpr(right, Operation.unaryPrefix(token), ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
				}
			}
			case LEFT_PARENTHESIS -> {
				if (reader.peak(1).type == Token.Type.COLON) {
					Position startPos = reader.position();
					reader.advance();
					reader.advance();
					ValueType type = getValueType();
					
					if (type == null) {
						throw createParseException(startPos, "Invalid cast type", reader.peakString(-1, 5));
					}
					
					tryMatchOrError(Token.Type.RIGHT_PARENTHESIS, () -> "Expected ')'");
					reader.advance();
					
					Expr right = unaryExpression();
					
					CommaExpr comma;
					if ((comma = getCommaExpr(right)) != null) {
						comma.setLast(
							new CastExpr(comma.getLast(), type, ISyntaxPosition.of(startPos, reader.lastPositionEnd()))
						);
						return right;
					} else {
						return new CastExpr(right, type, ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
					}
				}
			}
		}
		
		return atomExpression();
	}
	
	/**
	 * Returns an atom expression
	 *
	 * <pre>
	 * AtomExpression ::= IDENTIFIER
	 *   | NullExpression
	 *   | BooleanExpression
	 *   | FloatingExpression
	 *   | IntegerExpression
	 *   | CharacterExpression
	 *   | StringExpression
	 *   | Expression
	 * </pre>
	 */
	private Expr atomExpression() {
		switch (reader.type()) {
			case IDENTIFIER -> {
				// We always give the expression a variable reference but change it by it's caller
				// This way we do not need to modify the system but we will need to make sure that
				// the internal ProgramScope removes the invalid references if that is the case.
				//
				// We could also use the ProgramScope to generate Reference values and then add
				// them to the correct scope.
				
				// First we find the locals
				// Then we check the functions
				// Then we check the globals
				// After that we mark it as IMPORT
				Position startPos = reader.position();
				String name;
				{
					StringBuilder sb = new StringBuilder();
					
					while (reader.type() == Token.Type.IDENTIFIER) {
						sb.append(reader.value());
						reader.advance();
						
						if (reader.type() == Token.Type.NAMESPACE_OPERATOR) {
							sb.append("::");
							reader.advance();
							tryMatchOrError(Token.Type.IDENTIFIER);
						}
					}
					
					name = sb.toString();
				}
				
				Reference reference = currentScope.getLocalScope().getLocal(name);
				if (reference == null) {
					reference = currentScope.getFunctionScope().getFunction(name);
					if (reference == null) {
						reference = currentScope.getLocalScope().getVariable(name);
						if (reference == null) {
							reference = currentScope.createImportedReference(name);
						}
					}
				}
				
				if (reference != null) {
					// Increment the usage count
					reference.incUsages();
				}
				
				return new NameExpr(reference, ISyntaxPosition.of(startPos, reader.lastPositionEnd()));
			}
			case STRING -> {
				String value = reader.value();
				value = StringUtils.unescapeString(value.substring(1, value.length() - 1));
				Expr expr = new StrExpr(value, reader.syntaxPosition());
				reader.advance();
				return expr;
			}
			case CHARACTER -> {
				String value = StringUtils.unescapeString(reader.value().substring(1, reader.value().length() - 1));
				if (value.length() != 1) {
					throw createParseException("Characters can only contain one character");
				}
				Expr expr = new NumExpr(value.charAt(0) & 0xff, Atom.int_8, reader.syntaxPosition());
				reader.advance();
				return expr;
			}
			case NULL -> {
				Expr expr = new NullExpr(reader.syntaxPosition());
				reader.advance();
				return expr;
			}
			case BOOLEAN -> {
				Expr expr = new NumExpr(reader.value().equals("true"), reader.syntaxPosition());
				reader.advance();
				return expr;
			}
			case DOUBLE, FLOAT -> {
				throw createParseException("Floating point types are not implemented");
			}
			case LONG -> {
				Expr expr = new NumExpr(StringUtils.parseLong(reader.value()), Atom.int_64, reader.syntaxPosition());
				reader.advance();
				return expr;
			}
			case INT -> {
				Expr expr = new NumExpr(StringUtils.parseInteger(reader.value()), Atom.int_32, reader.syntaxPosition());
				reader.advance();
				return expr;
			}
			case LEFT_PARENTHESIS -> {
				reader.advance();
				Expr expr = expression();
				
				tryMatchOrError(Token.Type.RIGHT_PARENTHESIS);
				reader.advance();
				
				return expr;
			}
		}
		
		System.out.println(reader.token());
		System.out.println(reader.position().line);
		System.out.println(reader.position().column);
		throw createParseException("Invalid character");
	}
	
	private boolean isNamespaceDeclaration() {
		int prevIndex = reader.readerIndex();
		if (reader.type() == Token.Type.EXPORT) {
			reader.advance();
		}
		
		if (reader.type() == Token.Type.NAMESPACE
		&& reader.peak(1).type == Token.Type.IDENTIFIER) {
			reader.readerIndex(prevIndex);
			return true;
		}
		
		reader.readerIndex(prevIndex);
		return false;
	}
	
	private boolean isFunctionDeclaration() {
		int prevIndex = reader.readerIndex();
		if (reader.type() == Token.Type.EXPORT) {
			reader.advance();
		}
		
		ValueType type = getValueType();
		boolean result = type != null
			&& reader.type() == Token.Type.IDENTIFIER
			&& reader.peak(1).type == Token.Type.LEFT_PARENTHESIS;
		
		reader.readerIndex(prevIndex);
		return result;
	}
	
	private boolean isValueTypeAndName() {
		int prevIndex = reader.readerIndex();
		ValueType type = getValueType();
		
		if (type != null && reader.type() == Token.Type.IDENTIFIER) {
			reader.readerIndex(prevIndex);
			return true;
		}
		
		reader.readerIndex(prevIndex);
		return false;
	}
	
	private boolean isValueType() {
		int prevIndex = reader.readerIndex();
		ValueType type = getValueType();
		reader.readerIndex(prevIndex);
		return type != null;
	}
	
	private ValueType getValueType() {
		int prevIndex = reader.readerIndex();
		
		boolean isConst = false;
		boolean isVolatile = false;
		boolean isSigned = false;
		boolean isUnsigned = false;
		
		if (reader.type() == Token.Type.CONST) {
			isConst = true;
			reader.advance();
		}
		
		if (reader.type() == Token.Type.VOLATILE) {
			isVolatile = true;
			reader.advance();
		}
		
		switch (reader.type()) {
			case UNSIGNED -> {
				isUnsigned = true;
				reader.advance();
			}
			case SIGNED -> {
				isSigned = true;
				reader.advance();
			}
		}
		
		int size = 0;
		int flags = 0;
		switch (reader.type()) {
			case VOID_TYPE, BOOL_TYPE, CHAR_TYPE, INT_TYPE, LONG_TYPE, SHORT_TYPE, FLOAT_TYPE, DOUBLE_TYPE -> {
				ValueType primitive = Primitives.getPrimitive(reader.value());
				if (primitive != null) {
					size = primitive.getSize();
					flags = primitive.getFlags();
					
					if ((flags & ValueType.FLOATING) != 0) {
						// Throw error right now because floating values are not implemented yet
					}
				}
			}
			case IDENTIFIER -> {}
			default -> {
				reader.readerIndex(prevIndex);
				return null;
			}
		}
		
		if (isUnsigned || isSigned) {
			flags &= ~ValueType.UNSIGNED;
		}
		
		String name = reader.value();
		reader.advance();
		
		int depth = 0;
		while (reader.type() == Token.Type.MUL) {
			depth ++;
			reader.advance();
		}
		
		// TODO: Cache the value
		return new ValueType(name, size, depth,
			(isUnsigned ? ValueType.UNSIGNED : 0) |
			(isConst ? ValueType.CONST : 0) |
			(isVolatile ? ValueType.VOLATILE : 0) | flags
		);
	}
	
	/**
	 * Check that all lables are resolved inside the current scope
	 */
	private boolean popLablesAndErrorCheck() {
		for (GotoStat stat : currentScope.getLabelScope().getGotos()) {
			Reference gotoRef = stat.getReference();
			String name = gotoRef.getName();
			
			Reference reference = currentScope.getLabelScope().getLocalLabel(name);
			if (reference == null) {
				throw createParseException(stat.getSyntaxPosition().getStartPosition(), "The label '%s' was not defined", name);
			}
			
			if (gotoRef != reference) {
				gotoRef.decUsages();
				reference.incUsages();
			}
			
			// Update the goto reference
			stat.setReference(reference);
		}
		
		// Only resolve locals
		for (NameExpr expr : currentScope.getLabelScope().getMissingLabelReferences()) {
			Reference exprRef = expr.getReference();
			
			Reference reference = currentScope.getLabelScope().getLocalLabel(exprRef.getName());
			if (reference != null) {
				// Because we are removing the exprRef we decrement the usages
				if (exprRef != reference) {
					exprRef.decUsages();
					reference.incUsages();
				}
				
				expr.setReference(reference);
			} else {
				if (currentScope.getLabelScope().isGlobalLabelScope()) {
					// Imported function reference
					expr.getReference().setFlags(Reference.IMPORT | Reference.FUNCTION);
				} else {
					// Check for a function
					reference = currentScope.getFunctionScope().getLocalFunction(exprRef.getName());
					if (reference != null) {
						// Decrement the usage
						if (exprRef != reference) {
							exprRef.decUsages();
							reference.incUsages();
						}
						
						expr.setReference(reference);
					} else {
						// Global label or imported function
						currentScope.getLabelScope().addGlobalMissingLabelReference(expr);
					}
				}
			}
		}
		
		currentScope.getLabelScope().popBlock();
		
		return true;
	}
	
	private CommaExpr getCommaExpr(Expr expr) {
		CommaExpr lastComma = null;
		
		while (expr instanceof CommaExpr e) {
			lastComma = e;
			expr = e.getLast();
		}
		
		return lastComma;
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
