package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.context.LangReader;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.expr.NameExpr;
import me.hardcoded.compiler.parser.expr.NoneExpr;
import me.hardcoded.compiler.parser.expr.NumExpr;
import me.hardcoded.compiler.parser.scope.ProgramScope;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.*;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.lexer.LexerTokenizer;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.MutableSyntaxImpl;
import me.hardcoded.utils.ObjectUtils;
import me.hardcoded.utils.Position;

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
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
	private final ProgramScope context;
	private final List<String> importedFiles;
	
	// Configurable fields
	private CompilerConfiguration config;
	private LangReader reader;
	private File currentFile;
	
	public AmpleParser() {
		this.context = new ProgramScope();
		this.importedFiles = new ArrayList<>();
		
		// Add type block
		context.getTypeScope().pushBlock();
		context.getLocalScope().pushBlock();
		context.getFunctionScope().pushBlock();
		
		for (ValueType type : Primitives.VALUES) {
			context.getTypeScope().addLocalType(type);
		}
	}
	
	public LinkableObject fromFile(File file) throws IOException {
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			System.out.printf("Could not find the file '%s'\n", file.getAbsolutePath());
			throw e;
		}
		
		return fromBytes(file, bytes);
	}
	
	private LinkableObject fromBytes(File file, byte[] bytes) {
		if (bytes == null) {
			throw createParseException("Tried to parse an array 'null'");
		}
		
		LangReader oldContext = reader;
		File oldFile = currentFile;
		
		// Update the fields inside this class
		currentFile = file;
		reader = LangReader.wrap(LexerTokenizer.parse(file, bytes));
		
		// Parse the current code
		ProgStat program = parse();
		
		List<Reference> missingReferences = new ArrayList<>();
		List<Reference> exportedReferences = new ArrayList<>();
		for (Reference reference : context.getAllReferences()) {
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
		
		try {
			// System.out.println(ObjectUtils.deepPrint("Program", program, 16));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return linkableObject;
	}
	
	/**
	 * This is the start of the parsing
	 */
	private ProgStat parse() {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(reader.position(), null);
		ProgStat list = new ProgStat(mutableSyntax);
		
		while (reader.remaining() > 0) {
			Stat stat = parseStatement();
			
			// Remove empty statements
			if (!stat.isEmpty()) {
				list.addElement(stat);
			}
			
			System.out.println(stat);
		}
		
		mutableSyntax.end = reader.lastPositionEnd();
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
		
		if (reader.type() == Token.Type.FUNC) {
			return funcStatement();
		}
		
		if (isType()) {
			return varStatement();
		}
		
		throw createParseException(reader.position(), "Invalid statement");
	}
	
	private Stat funcStatement() {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(reader.position(), null);
		reader.advance();
		
		tryMatchOrError(Token.Type.IDENTIFIER);
		String functionName = reader.value();
		reader.advance();
		
		tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		context.getLocalScope().pushBlock();
		
		List<Reference> parameters = new ArrayList<>();
		while (reader.type() != Token.Type.R_PAREN) {
			ValueType type = context.getTypeScope().getType(reader.value());
			if (type == null) {
				throw createParseException("Missing type '%s' was not a type", reader.value());
			}
			
			reader.advance();
			
			tryMatchOrError(Token.Type.COLON);
			reader.advance();
			
			Reference reference = context.getLocalScope().addLocalVariable(type, reader.value());
			parameters.add(reference);
			reader.advance();
			
			if (reader.type() == Token.Type.COMMA) {
				reader.advance();
				if (reader.type() == Token.Type.R_PAREN) {
					throw createParseException("Invalid comma before ')'");
				}
			} else {
				break;
			}
		}
		
		tryMatchOrError(Token.Type.R_PAREN, () -> "Missing parameter separator");
		reader.advance();
		
		tryMatchOrError(Token.Type.COLON, () -> "Missing function return value");
		reader.advance();
		
		ValueType returnType = context.getTypeScope().getType(reader.value());
		reader.advance();
		
		Reference reference = context.getFunctionScope().addFunction(returnType, functionName, parameters);
		FuncStat stat = new FuncStat(mutableSyntax, parameters, reference);
		
		tryMatchOrError(Token.Type.L_CURLY, () -> "Missing function body");
		stat.setBody(statements());
		
		mutableSyntax.end = reader.lastPositionEnd();
		
		context.getLocalScope().popLocals();
		
		parameters.forEach(System.out::println);
		
		return stat;
	}
	
	private ScopeStat statements() {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(reader.position(), null);
		ScopeStat stat = new ScopeStat(mutableSyntax);
		reader.advance();
		
		context.getLocalScope().pushLocals();
		
		while (reader.type() != Token.Type.R_CURLY) {
			Stat element = statement();
			
			if (!element.isEmpty()) {
				stat.addElement(element);
			}
		}
		
		context.getLocalScope().popLocals();
		
		tryMatchOrError(Token.Type.R_CURLY, () -> "Unclosed scope. Missing '}'");
		reader.advance();
		
		mutableSyntax.end = reader.lastPositionEnd();
		return stat;
	}
	
	private Stat statement() {
		if (reader.type() == Token.Type.L_CURLY) {
			return statements();
		}
		
		if (isType()) {
			return varStatement();
		}
		
		switch (reader.type()) {
			case IF -> { return ifStatement(); }
			case WHILE -> {}
			case FOR -> {}
			case IDENTIFIER -> {}
		}
		
		Stat stat = switch (reader.type()) {
			case RETURN -> {
				Position startPos = reader.position();
				reader.advance();
				
				Expr expr;
				if (reader.type() != Token.Type.SEMICOLON) {
					expr = expression();
				} else {
					expr = new NoneExpr(ISyntaxPosition.of(reader.position(), reader.position()));
				}
				
				yield new ReturnStat(ISyntaxPosition.of(startPos, reader.nextPositionEnd()), expr);
			}
			default -> expression();
		};
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		return stat;
	}
	
	private IfStat ifStatement() {
		Position startPos = reader.position();
		reader.advance();
		
		tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		Expr value = expression();
		
		tryMatchOrError(Token.Type.R_PAREN);
		reader.advance();
		
		Stat body = statements();
		Stat elseBody;
		if (reader.type() == Token.Type.ELSE) {
			reader.advance();
			elseBody = statements();
		} else {
			elseBody = new EmptyStat(reader.syntaxPosition());
		}
		
		return new IfStat(ISyntaxPosition.of(startPos, elseBody.getSyntaxPosition().getEndPosition()), value, body, elseBody);
	}
	
	private VarStat varStatement() {
		Position startPos = reader.position();
		
		ValueType type = context.getTypeScope().getType(reader.value());
		reader.advance();
		
		tryMatchOrError(Token.Type.COLON);
		reader.advance();
		
		tryMatchOrError(Token.Type.IDENTIFIER);
		Position namePos = reader.position();
		String name = reader.value();
		reader.advance();
		
		tryMatchOrError(Token.Type.ASSIGN);
		reader.advance();
		
		Expr value = expression();
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		// This is only allowed if the variable is global
		// But for now we will prohibit any overwriting
		
		if (context.getLocalScope().getVariable(name) != null) {
			throw createParseException(namePos, "A variable '%s' has already been defined", name);
		}
		
		Reference reference = context.getLocalScope().addLocalVariable(type, name);
		return new VarStat(ISyntaxPosition.of(startPos, reader.lastPositionEnd()), reference, value);
	}
	
	// Shunting yard algorithm
	private Expr expression() {
		return expression(false);
	}
	
	private Expr expression(boolean allowComma) {
		Expr expr = new ExprParser(context, reader).parse(allowComma);
		
		System.out.println(expr);
		
		return expr;
	}
	
	private boolean isType() {
		return context.getTypeScope().getType(reader.value()) != null;
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
