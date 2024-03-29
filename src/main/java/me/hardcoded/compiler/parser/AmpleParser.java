package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.context.LangReader;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.expr.NoneExpr;
import me.hardcoded.compiler.parser.expr.NumExpr;
import me.hardcoded.compiler.parser.scope.ProgramScope;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.*;
import me.hardcoded.lexer.LexerTokenizer;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.AmpleCache;
import me.hardcoded.utils.MutableSyntaxImpl;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.error.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class is responsible for creating the abstract syntax tree of the arucas programming language.
 * No optimizations should be applied in this parser and no type checking should be done.
 * <p>
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
	private static final Logger LOGGER = LogManager.getLogger(AmpleParser.class);
	private final ProgramScope context;
	private final List<String> importedFiles;
	private final AmpleConfig ampleConfig;
	
	// Configurable fields
	private LangReader reader;
	private File currentFile;
	private String fileContent;
	private boolean repl;
	
	public AmpleParser(AmpleConfig ampleConfig) {
		this.context = new ProgramScope();
		this.importedFiles = new ArrayList<>();
		this.ampleConfig = ampleConfig;
		
		// Add type block
		context.getTypeScope().pushBlock();
		context.getLocalScope().pushBlock();
		context.getFunctionScope().pushBlock();
		
		for (ValueType type : Primitives.VALUES) {
			context.getTypeScope().addLocalType(type);
		}
	}
	
	public File getCurrentFile() {
		return currentFile;
	}
	
	public LinkableObject fromFile(File file) throws ParseException, IOException {
		//			LOGGER.error("Could not find the file '{}'", file.getAbsolutePath());
		//			throw new ParseException("Failed to read file '%s'", file.getAbsolutePath());
		return fromBytes(file.getAbsolutePath(), Files.readAllBytes(file.toPath()));
	}
	
	public LinkableObject fromReplBytes(String path, byte[] bytes) throws ParseException {
		repl = true;
		return fromBytes(path, bytes);
	}
	
	private LinkableObject fromBytes(String path, byte[] bytes) throws ParseException {
		if (bytes == null) {
			throw createParseException("Tried to parse an array 'null'");
		}
		
		// Update the fields inside this class
		currentFile = new File(path);
		fileContent = new String(bytes);
		reader = LangReader.wrap(currentFile, LexerTokenizer.parse(path, bytes));
		
		// Parse the current code
		ProgStat program = parse();
		
		List<ReferenceSyntax> importedReferences = new ArrayList<>();
		List<ReferenceSyntax> exportedReferences = new ArrayList<>();
		for (Reference reference : context.getAllReferences()) {
			if (reference.isImported()) {
				importedReferences.add(new ReferenceSyntax(reference, context.getFirstReferencePosition(reference)));
			}
			
			if (reference.isExported()) {
				exportedReferences.add(new ReferenceSyntax(reference, context.getFirstReferencePosition(reference)));
			}
		}
		
		LOGGER.debug("");
		LOGGER.debug("References:");
		LOGGER.debug("  imported = {}", importedReferences);
		LOGGER.debug("  exported = {}", exportedReferences);
		
		AmpleCache.putFileSource(currentFile, new String(bytes));
		String fileChecksum = AmpleCache.getDataChecksum(bytes);
		LinkableObject linkableObject = new LinkableObject(currentFile, fileChecksum, program, importedFiles, exportedReferences, importedReferences);
		
		if (reader.remaining() != 0) {
			throw createParseException(reader.syntaxPosition(), "Failed to parse file fully");
		}
		
		return linkableObject;
	}
	
	/**
	 * This is the start of the parsing
	 */
	private ProgStat parse() throws ParseException {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(currentFile.getAbsolutePath(), reader.position(), null);
		ProgStat list = new ProgStat(mutableSyntax);
		
		while (reader.remaining() > 0) {
			Stat stat = parseStatement(true);
			
			// Remove empty statements
			if (!stat.isEmpty()) {
				list.addElement(stat);
			}
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
	private Stat parseStatement(boolean allowLink) throws ParseException {
		if (allowLink && reader.type() == Token.Type.LINK) {
			Position startPos = reader.position();
			reader.advance();
			
			tryMatchOrError(Token.Type.STRING);
			String importedFile = reader.value();
			
			importedFiles.add(importedFile.substring(1, importedFile.length() - 1));
			reader.advance();
			
			// TODO: Import types and structs of file loaded
			
			tryMatchOrError(Token.Type.SEMICOLON);
			reader.advance();
			
			return new EmptyStat(ISyntaxPos.of(currentFile, startPos, reader.lastPositionEnd()));
		}
		
		if (isNamespace()) {
			return namespaceStatement();
		}
		
		if (isFunc()) {
			return funcStatement();
		}
		
		if (repl) {
			// REPL (Read Eval Print Loop)
			// Scopes should work for REPL
			
			return replStatements();
		} else {
			if (isType()) {
				return varStatement(false);
			}
		}
		
		throw createParseException(reader.syntaxPosition(), "Invalid statement");
	}
	
	private Stat replStatements() throws ParseException {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(currentFile.getAbsolutePath(), reader.position(), null);
		ScopeStat stat = new ScopeStat(mutableSyntax);
		
		while (reader.remaining() > 0) {
			Stat element = statement();
			
			if (element instanceof VarStat var) {
				// All direct variables are exported
				var.getReference().setExported(true);
			}
			
			if (!element.isEmpty()) {
				stat.addElement(element);
			}
		}
		
		mutableSyntax.end = reader.lastPositionEnd();
		return stat;
	}
	
	private Stat namespaceStatement() throws ParseException {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(currentFile.getAbsolutePath(), reader.position(), null);
		reader.advance();
		
		//		int modifiers = 0;
		//		if (reader.type() == Token.Type.L_PAREN) {
		//			reader.advance();
		//
		//			while (reader.type() != Token.Type.R_PAREN) {
		//				switch (reader.type()) {
		//					case EXPORT -> {
		//						modifiers |= Reference.EXPORT;
		//					}
		//					default -> {
		//						throw createParseException(reader.syntaxPosition(), "Invalid namespace modifier '%s'", reader.value());
		//					}
		//				}
		//				reader.advance();
		//			}
		//
		//			tryMatchOrError(Token.Type.R_PAREN);
		//			reader.advance();
		//		}
		
		int namespaceCount = 0;
		do {
			tryMatchOrError(Token.Type.IDENTIFIER);
			String namespaceName = reader.value();
			reader.advance();
			
			context.getNamespaceScope().pushNamespace(namespaceName);
			namespaceCount++;
			
			if (reader.type() == Token.Type.L_CURLY) {
				break;
			}
			
			tryMatchOrError(Token.Type.NAMESPACE_OPERATOR);
			reader.advance();
		} while (true);
		
		tryMatchOrError(Token.Type.L_CURLY);
		reader.advance();
		
		Reference namespace = context.getNamespaceScope().getNamespaceReference();
		NamespaceStat stat = new NamespaceStat(mutableSyntax, namespace);
		
		while (reader.type() != Token.Type.R_CURLY) {
			Stat element = parseStatement(false);
			
			//			// Apply modifiers
			//			if ((modifiers & Reference.EXPORT) != 0) {
			//				if (element instanceof FuncStat funcStat) {
			//					funcStat.getReference().setExported(true);
			//				}
			//			}
			
			// Remove empty statements
			if (!element.isEmpty()) {
				stat.addElement(element);
			}
		}
		
		tryMatchOrError(Token.Type.R_CURLY);
		reader.advance();
		
		mutableSyntax.end = reader.lastPositionEnd();
		
		for (int i = 0; i < namespaceCount; i++) {
			context.getNamespaceScope().popNamespace();
		}
		
		return stat;
	}
	
	private Stat funcStatement() throws ParseException {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(currentFile.getAbsolutePath(), reader.position(), null);
		reader.advance();
		
		int modifiers = 0;
		if (reader.type() == Token.Type.L_PAREN) {
			reader.advance();
			
			while (reader.type() != Token.Type.R_PAREN) {
				switch (reader.type()) {
					case EXPORT -> {
						modifiers |= Reference.EXPORT;
					}
					default -> {
						throw createParseException(reader.syntaxPosition(), "Invalid function modifier '%s'", reader.value());
					}
				}
				reader.advance();
			}
			
			tryMatchOrError(Token.Type.R_PAREN);
			reader.advance();
		}
		
		if (repl) {
			modifiers |= Reference.EXPORT;
		}
		
		tryMatchOrError(Token.Type.IDENTIFIER);
		ISyntaxPos functionNameSyntax = reader.syntaxPosition();
		String functionName = reader.value();
		reader.advance();
		
		tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		context.getLocalScope().pushBlock();
		context.getLocalScope().pushLocals();
		
		List<Reference> parameters = new ArrayList<>();
		while (reader.type() != Token.Type.R_PAREN) {
			ValueType type = readType();
			if (type == null) {
				throw createParseException("Missing type '%s' was not a type", reader.value());
			}
			
			tryMatchOrError(Token.Type.COLON);
			reader.advance();
			
			Reference reference = context.getLocalScope().addLocalVariable(context.getNamespaceScope().getNamespaceRoot(), type, reader.value());
			parameters.add(reference);
			reader.advance();
			
			if (reader.type() == Token.Type.COMMA) {
				if (type.isVarargs()) {
					throw createParseException(reader.peak(1).syntaxPosition, "Cannot add parameters after vararg");
				}
				
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
		
		ValueType returnType;
		if (reader.type() == Token.Type.COLON) {
			reader.advance();
			returnType = readType();
		} else {
			returnType = Primitives.NONE;
		}
		
		Reference reference = context.getFunctionScope().addFunction(returnType, context.getNamespaceScope().getNamespace(), functionName, parameters);
		if (reference == null) {
			Reference blocker = context.getFunctionScope().getFunctionBlocking(context.getNamespaceScope().getNamespace(), functionName, parameters);
			ISyntaxPos syntaxPosition = context.getFirstReferencePosition(blocker);
			Position startPos = syntaxPosition == null ? null : syntaxPosition.getStartPosition();
			
			throw createParseException(
				functionNameSyntax,
				"A function with the name '%s' already exists (%s) (line: %s, column: %s)",
				functionName,
				AmpleMangler.demangleFunction(blocker.getMangledName()),
				startPos == null ? "?" : (startPos.line() + 1),
				startPos == null ? "?" : (startPos.column() + 1)
			);
		}
		reference.setModifiers(modifiers);
		
		// Always set reference position
		context.setReferencePosition(reference, functionNameSyntax);
		
		FuncStat stat = new FuncStat(mutableSyntax, parameters, reference);
		
		tryMatchOrError(Token.Type.L_CURLY, () -> "Missing function body");
		stat.setBody(statements());
		
		mutableSyntax.end = reader.lastPositionEnd();
		
		context.getLocalScope().popLocals();
		
		return stat;
	}
	
	private ScopeStat statements() throws ParseException {
		MutableSyntaxImpl mutableSyntax = new MutableSyntaxImpl(currentFile.getAbsolutePath(), reader.position(), null);
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
	
	private Stat statement() throws ParseException {
		if (reader.type() == Token.Type.L_CURLY) {
			return statements();
		}
		
		if (isType()) {
			return varStatement(true);
		}
		
		switch (reader.type()) {
			case IF -> {
				return ifStatement();
			}
			case FOR -> {
				return forStatement();
			}
			case WHILE -> {
				return whileStatement();
			}
		}
		
		Stat stat = switch (reader.type()) {
			case RETURN -> {
				Position startPos = reader.position();
				reader.advance();
				
				Expr expr;
				if (reader.type() != Token.Type.SEMICOLON) {
					expr = expression();
				} else {
					expr = new NoneExpr(ISyntaxPos.of(currentFile, reader.position(), reader.position()));
				}
				
				yield new ReturnStat(ISyntaxPos.of(currentFile, startPos, reader.nextPositionEnd()), expr);
			}
			case CONTINUE -> {
				Position startPos = reader.position();
				reader.advance();
				yield new ContinueStat(ISyntaxPos.of(currentFile, startPos, reader.nextPositionEnd()));
			}
			case BREAK -> {
				Position startPos = reader.position();
				reader.advance();
				yield new BreakStat(ISyntaxPos.of(currentFile, startPos, reader.nextPositionEnd()));
			}
			case COMPILER -> compilerStatement();
			default -> expression();
		};
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		return stat;
	}
	
	private CompilerStat compilerStatement() throws ParseException {
		Position startPos = reader.position();
		reader.advance();
		
		tryMatchOrError(Token.Type.LESS_THAN);
		reader.advance();
		
		String targetType = reader.value();
		reader.advance();
		
		tryMatchOrError(Token.Type.MORE_THAN);
		reader.advance();
		
		tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		List<CompilerStat.Part> parts = new ArrayList<>();
		
		// TODO: Read strings until R_PAREN
		while (reader.type() != Token.Type.R_PAREN) {
			tryMatchOrError(Token.Type.STRING);
			
			Position partStartPos = reader.position();
			
			// TODO: Create a utility method for this
			String command = reader.value();
			command = command.substring(1, command.length() - 1);
			reader.advance();
			
			List<Reference> references = new ArrayList<>();
			while (reader.type() == Token.Type.COLON) {
				reader.advance();
				
				Namespace namespace = readNamespace();
				Reference reference = context.getLocalScope().getVariable(namespace, reader.value());
				if (reference == null) {
					throw createParseException("Could not find the variable '%s'", reader.value());
				}
				reader.advance();
				references.add(reference);
			}
			
			parts.add(new CompilerStat.Part(ISyntaxPos.of(currentFile, partStartPos, reader.lastPositionEnd()), command, references));
		}
		
		tryMatchOrError(Token.Type.R_PAREN);
		reader.advance();
		
		return new CompilerStat(ISyntaxPos.of(currentFile, startPos, reader.lastPositionEnd()), targetType, parts);
	}
	
	private ForStat forStatement() throws ParseException {
		Position startPos = reader.position();
		reader.advance();
		
		tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		Stat initializer = varStatement(true);
		
		Expr condition;
		if (reader.type() != Token.Type.SEMICOLON) {
			condition = expression();
		} else {
			condition = new NumExpr(reader.syntaxPosition(), Primitives.I32, 1);
		}
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		Expr action;
		if (reader.type() != Token.Type.R_PAREN) {
			action = expression();
		} else {
			action = new NoneExpr(reader.syntaxPosition());
		}
		
		tryMatchOrError(Token.Type.R_PAREN);
		reader.advance();
		
		ScopeStat body = statements();
		
		return new ForStat(ISyntaxPos.of(currentFile, startPos, body.getSyntaxPosition().getEndPosition()), initializer, condition, action, body);
	}
	
	private WhileStat whileStatement() throws ParseException {
		Position startPos = reader.position();
		reader.advance();
		
		tryMatchOrError(Token.Type.L_PAREN);
		reader.advance();
		
		Expr condition = expression();
		
		tryMatchOrError(Token.Type.R_PAREN);
		reader.advance();
		
		ScopeStat body = statements();
		
		return new WhileStat(ISyntaxPos.of(currentFile, startPos, body.getSyntaxPosition().getEndPosition()), condition, body);
	}
	
	private IfStat ifStatement() throws ParseException {
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
			elseBody = statement();
		} else {
			elseBody = new EmptyStat(reader.syntaxPosition());
		}
		
		return new IfStat(ISyntaxPos.of(currentFile, startPos, elseBody.getSyntaxPosition().getEndPosition()), value, body, elseBody);
	}
	
	private VarStat varStatement(boolean localVariable) throws ParseException {
		Position startPos = reader.position();
		
		ISyntaxPos typeSyntaxPosition = reader.syntaxPosition();
		ValueType type = readType();
		if (type == null) {
			throw createParseException(typeSyntaxPosition, "Unknown type");
		}
		
		tryMatchOrError(Token.Type.COLON);
		reader.advance();
		
		tryMatchOrError(Token.Type.IDENTIFIER);
		ISyntaxPos namePos = reader.syntaxPosition();
		String name = reader.value();
		reader.advance();
		
		tryMatchOrError(Token.Type.ASSIGN);
		reader.advance();
		
		Expr value = expression();
		
		tryMatchOrError(Token.Type.SEMICOLON);
		reader.advance();
		
		Namespace namespace;
		if (localVariable) {
			namespace = context.getNamespaceScope().getNamespaceRoot();
		} else {
			namespace = context.getNamespaceScope().getNamespace();
		}
		
		if (context.getLocalScope().getVariable(namespace, name) != null) {
			throw createParseException(namePos, "A %s variable '%s' has already been defined", localVariable ? "local" : "global", name);
		}
		
		Reference reference = context.getLocalScope().addLocalVariable(namespace, type, name);
		return new VarStat(ISyntaxPos.of(currentFile, startPos, reader.lastPositionEnd()), reference, value);
	}
	
	// Shunting yard algorithm
	private Expr expression() throws ParseException {
		return new ExprParser(this, context, reader).parse();
	}
	
	boolean isType() {
		return context.getTypeScope().getType(reader.value(), 0) != null;
	}
	
	boolean isFunc() {
		return reader.type() == Token.Type.FUNC;
	}
	
	boolean isNamespace() {
		return reader.type() == Token.Type.NAMESPACE;
	}
	
	boolean isModifier() {
		return switch (reader.type()) {
			case EXPORT -> true;
			default -> false;
		};
	}
	
	ValueType readType() throws ParseException {
		String name = reader.value();
		reader.advance();
		int depth = 0;
		while (reader.type() == Token.Type.L_SQUARE) {
			reader.advance();
			tryMatchOrError(Token.Type.R_SQUARE);
			reader.advance();
			depth++;
		}
		
		return context.getTypeScope().getType(name, depth);
	}
	
	public Namespace readNamespace() throws ParseException {
		List<String> namespaceParts = new ArrayList<>();
		while (reader.peak(1).type == Token.Type.NAMESPACE_OPERATOR) {
			tryMatchOrError(Token.Type.IDENTIFIER);
			namespaceParts.add(reader.value());
			reader.advance();
			reader.advance();
		}
		
		Namespace namespace = context.getNamespaceScope().resolveNamespace(namespaceParts);
		if (namespace == null) {
			namespace = context.getNamespaceScope().importNamespace(namespaceParts);
		}
		
		return namespace;
	}
	
	ParseException createParseException(String format, Object... args) {
		return createParseException(reader == null ? null : reader.syntaxPosition(), format, args);
	}
	
	ParseException createParseException(ISyntaxPos syntaxPosition, String format, Object... args) {
		return createParseException(syntaxPosition.getPath(), syntaxPosition, format, args);
	}
	
	ParseException createParseException(String path, ISyntaxPos syntaxPosition, String format, Object... args) {
		String msg = String.format(format, args);
		
		StringBuilder sb = new StringBuilder();
		if (path == null) {
			sb.append("(?) ");
		} else {
			sb.append("(").append(path).append(") ");
		}
		
		if (syntaxPosition == null) {
			sb.append("(line: ?, column: ?): ").append(msg);
		} else {
			Position position = syntaxPosition.getStartPosition();
			sb.append("(line: ").append(position.line() + 1).append(", column: ").append(position.column() + 1).append("): ")
				.append(ErrorUtil.createError(syntaxPosition, fileContent, msg));
		}
		
		return new ParseException(sb.toString());
	}
	
	boolean tryMatchOrError(Token.Type type) throws ParseException {
		return tryMatchOrError(type, () -> "Expected %s but got %s".formatted(type, reader.type()));
	}
	
	boolean tryMatchOrError(Token.Type type, Supplier<String> message) throws ParseException {
		if (reader.type() != type) {
			throw createParseException(reader.syntaxPosition(), message.get());
		}
		
		return true;
	}
}
